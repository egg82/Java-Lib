package ninja.egg82.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang.StringUtils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import ninja.egg82.patterns.tuples.pair.Double2Pair;
import ninja.egg82.patterns.tuples.pair.Int2Pair;
import ninja.egg82.patterns.tuples.pair.IntIntPair;
import ninja.egg82.patterns.tuples.pair.Pair;

public class LanguageDatabase {
	//vars
	private DoubleMetaphone dm = new DoubleMetaphone();
	
	private List<List<String>> rows = new ArrayList<List<String>>(); // Plaintext table
	private List<List<String>> ciRows = new ArrayList<List<String>>(); // Case-insensitive table
	private List<List<String>> dmRows = new ArrayList<List<String>>(); // DM table
	private Map<String, IntList> containsRows = new ConcurrentHashMap<String, IntList>(); // Needed for fast exact & contains matches
	private Map<String, IntList> containsCiRows = new ConcurrentHashMap<String, IntList>(); // Case-insensitive version
	private Map<String, IntList> containsDmRows = new ConcurrentHashMap<String, IntList>(); // There may be multiple rows with the same values, hence the list
	
	private Cache<String, List<Pair<String, IntIntPair>>> exactCache = Caffeine.newBuilder().maximumSize(1000L).build(); // Last 1000 single-word searches
	private Cache<String, List<Pair<String, IntIntPair>>> containsCache = Caffeine.newBuilder().maximumSize(1000L).build(); // Last 1000 single-word searches
	private Cache<String, List<Pair<String, IntIntPair>>> levenshteinCache = Caffeine.newBuilder().maximumSize(1000L).build(); // Last 1000 single-word searches
	private Cache<String, List<Pair<String, IntIntPair>>> dmCache = Caffeine.newBuilder().maximumSize(1000L).build(); // Last 1000 single-word searches
	
	//constructor
	public LanguageDatabase() {
		dm.setMaxCodeLen(10);
	}
	
	//public
	/**
	 * Adds a row to the database and returns its index (row ID).
	 * 
	 * @param columns Any columns to give the row. The number of columns does not have to match any previous rows given
	 * @return The row ID/index of the newly-added columns
	 */
	public synchronized int addRow(String... columns) {
		if (columns == null) {
			throw new IllegalArgumentException("columns cannot be null.");
		}
		
		columns = stripBlanksAndDuplicates(columns);
		if (columns.length == 0) {
			return -1;
		}
		
		int index = rows.size();
		
		List<String> cs = Arrays.asList(columns);
		List<String> ci = Arrays.asList(columns);
		List<String> dm = generateDm(columns);
		
		for (int i = 0; i < ci.size(); i++) {
			ci.set(i, ci.get(i).toLowerCase());
		}
		
		rows.add(cs);
		ciRows.add(ci);
		dmRows.add(dm);
		
		for (int i = 0; i < cs.size(); i++) {
			String csString = cs.get(i);
			String ciString = ci.get(i);
			String dmString = dm.get(i);
			
			if (containsRows.containsKey(csString)) {
				IntList values = containsRows.get(csString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsRows.put(csString, new IntArrayList(new int[] { index }));
			}
			if (containsCiRows.containsKey(ciString)) {
				IntList values = containsCiRows.get(ciString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsCiRows.put(ciString, new IntArrayList(new int[] { index }));
			}
			if (containsDmRows.containsKey(dmString)) {
				IntList values = containsDmRows.get(dmString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsDmRows.put(dmString, new IntArrayList(new int[] { index }));
			}
		}
		
		exactCache.invalidateAll();
		containsCache.invalidateAll();
		levenshteinCache.invalidateAll();
		dmCache.invalidateAll();
		
		return index;
	}
	/**
	 * Removes a row by the given index.
	 * 
	 * @param rowIndex The row ID to retrieve
	 */
	public synchronized void removeRow(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			return;
		}
		
		rows.remove(rowIndex);
		ciRows.remove(rowIndex);
		dmRows.remove(rowIndex);
		
		removeFromMap(containsRows, rowIndex);
		removeFromMap(containsCiRows, rowIndex);
		removeFromMap(containsDmRows, rowIndex);
		
		exactCache.invalidateAll();
		containsCache.invalidateAll();
		levenshteinCache.invalidateAll();
		dmCache.invalidateAll();
	}
	/**
	 * Returns the number of rows currently in the database.
	 * 
	 * @return The number of rows currently in the database
	 */
	public synchronized int numRows() {
		return rows.size();
	}
	/**
	 * Returns the number of columns for a given row in the database.
	 * 
	 * @param rowIndex The row ID
	 * @return The number of columns in the specified row ID
	 */
	public synchronized int numColumns(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			return -1;
		}
		return rows.get(rowIndex).size();
	}
	
	/**
	 * Returns the row IDs of any exact string matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] exact(String search, boolean caseSensitive) {
		return exact(new String[] {search}, caseSensitive);
	}
	/**
	 * Returns the row IDs of any exact string matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @param delimiter An optional delimiter to split the string into several searches
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] exact(String search, boolean caseSensitive, char delimiter) {
		return exact(search.split("\\" + delimiter), caseSensitive);
	}
	/**
	 * Returns the row IDs of any exact string matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] exact(String[] search, boolean caseSensitive) {
		IntList ids = new IntArrayList();
		ArrayList<Int2Pair<String>> keyColumn = new ArrayList<Int2Pair<String>>();
		ArrayList<Double2Pair<String>> keyScore = new ArrayList<Double2Pair<String>>();
		double maxColumn = 0.0d;
		double maxSize = 0.0d;
		double maxFrequency = 0.0d;
		
		if (!caseSensitive) {
			for (int i = 0; i < search.length; i++) {
				search[i] = search[i].toLowerCase();
			}
		}
		
		HashSet<String> searchSet = new HashSet<String>(Arrays.asList(search));
		searchSet.remove(null);
		searchSet.remove("");
		
		if (caseSensitive) {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = exactCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyColumn.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentColumn = result.get(i).getRight().getLeft();
						if (maxColumn < currentColumn) {
							maxColumn = currentColumn;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsRows.entrySet()) {
						String key = kvp.getKey();
						if (s.equals(key)) {
							int currentColumn = Integer.MAX_VALUE;
							for (int id : kvp.getValue()) {
								List<String> row = rows.get(id);
								for (int i = 0; i < row.size(); i++) {
									if (s.equals(row.get(i))) {
										if (i < currentColumn) {
											currentColumn = i;
										}
									}
								}
							}
							
							keyColumn.add(new Int2Pair<String>(key, currentColumn));
							if (maxColumn < currentColumn) {
								maxColumn = currentColumn;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(exactCache, s, key, currentColumn, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyColumn.size(); i++) {
				int frequency = Collections.frequency(keyColumn, keyColumn.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyColumn.size(); i++) {
				double score = keyColumn.get(i).getRight() / maxColumn;
				score -= (containsRows.get(keyColumn.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyColumn, keyColumn.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyColumn.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = exactCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyColumn.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentColumn = result.get(i).getRight().getLeft();
						if (maxColumn < currentColumn) {
							maxColumn = currentColumn;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsCiRows.entrySet()) {
						String key = kvp.getKey();
						if (key.contains(s) || s.contains(key)) {
							int currentColumn = Integer.MAX_VALUE;
							for (int id : kvp.getValue()) {
								List<String> row = ciRows.get(id);
								for (int i = 0; i < row.size(); i++) {
									if (s.equals(row.get(i))) {
										if (i < currentColumn) {
											currentColumn = i;
										}
									}
								}
							}
							
							keyColumn.add(new Int2Pair<String>(key, currentColumn));
							if (maxColumn < currentColumn) {
								maxColumn = currentColumn;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(exactCache, s, key, currentColumn, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyColumn.size(); i++) {
				int frequency = Collections.frequency(keyColumn, keyColumn.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyColumn.size(); i++) {
				double score = keyColumn.get(i).getRight() / maxColumn;
				score -= (containsCiRows.get(keyColumn.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyColumn, keyColumn.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyColumn.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Double2Pair<String>>() {
			@Override
			public int compare(Double2Pair<String> one, Double2Pair<String> two) {
				return Double.compare(one.getRight(), two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		}
		
		return ids.toIntArray();
	}
	
	/**
	 * Returns the row IDs of any substring matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] substring(String search, boolean caseSensitive) {
		return substring(new String[] {search}, caseSensitive);
	}
	/**
	 * Returns the row IDs of any substring matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @param delimiter An optional delimiter to split the string into several searches
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] substring(String search, boolean caseSensitive, char delimiter) {
		return substring(search.split("\\" + delimiter), caseSensitive);
	}
	/**
	 * Returns the row IDs of any substring matches and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] substring(String[] search, boolean caseSensitive) {
		IntList ids = new IntArrayList();
		ArrayList<Int2Pair<String>> keyLevenshtein = new ArrayList<Int2Pair<String>>();
		ArrayList<Double2Pair<String>> keyScore = new ArrayList<Double2Pair<String>>();
		double maxLevenshtein = 0.0d;
		double maxSize = 0.0d;
		double maxFrequency = 0.0d;
		
		if (!caseSensitive) {
			for (int i = 0; i < search.length; i++) {
				search[i] = search[i].toLowerCase();
			}
		}
		
		HashSet<String> searchSet = new HashSet<String>(Arrays.asList(search));
		searchSet.remove(null);
		searchSet.remove("");
		
		if (caseSensitive) {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = containsCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentDistance = result.get(i).getRight().getLeft();
						if (maxLevenshtein < currentDistance) {
							maxLevenshtein = currentDistance;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsRows.entrySet()) {
						String key = kvp.getKey();
						if (key.contains(s) || s.contains(key)) {
							int currentDistance = StringUtils.getLevenshteinDistance(key, s);
							keyLevenshtein.add(new Int2Pair<String>(key, currentDistance));
							if (maxLevenshtein < currentDistance) {
								maxLevenshtein = currentDistance;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(containsCache, s, key, currentDistance, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				int frequency = Collections.frequency(keyLevenshtein, keyLevenshtein.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = keyLevenshtein.get(i).getRight() / maxLevenshtein;
				score -= (containsRows.get(keyLevenshtein.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyLevenshtein.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = containsCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentDistance = result.get(i).getRight().getLeft();
						if (maxLevenshtein < currentDistance) {
							maxLevenshtein = currentDistance;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsCiRows.entrySet()) {
						String key = kvp.getKey();
						if (key.contains(s) || s.contains(key)) {
							int currentDistance = StringUtils.getLevenshteinDistance(key, s);
							keyLevenshtein.add(new Int2Pair<String>(key, currentDistance));
							if (maxLevenshtein < currentDistance) {
								maxLevenshtein = currentDistance;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(containsCache, s, key, currentDistance, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				int frequency = Collections.frequency(keyLevenshtein, keyLevenshtein.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = keyLevenshtein.get(i).getRight() / maxLevenshtein;
				score -= (containsCiRows.get(keyLevenshtein.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyLevenshtein.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Double2Pair<String>>() {
			@Override
			public int compare(Double2Pair<String> one, Double2Pair<String> two) {
				return Double.compare(one.getRight(), two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		}
		
		return ids.toIntArray();
	}
	
	/**
	 * Returns the row IDs of any levenshtein distances <= 3 and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] levenshtein(String search, boolean caseSensitive) {
		return levenshtein(new String[] {search}, caseSensitive);
	}
	/**
	 * Returns the row IDs of any levenshtein distances <= 3 and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @param delimiter An optional delimiter to split the string into several searches
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] levenshtein(String search, boolean caseSensitive, char delimiter) {
		return levenshtein(search.split("\\" + delimiter), caseSensitive);
	}
	/**
	 * Returns the row IDs of any levenshtein distances <= 3 and sorts the results by a relevance score descending and ID ascending.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] levenshtein(String[] search, boolean caseSensitive) {
		IntList ids = new IntArrayList();
		ArrayList<Int2Pair<String>> keyLevenshtein = new ArrayList<Int2Pair<String>>();
		ArrayList<Double2Pair<String>> keyScore = new ArrayList<Double2Pair<String>>();
		double maxLevenshtein = 0.0d;
		double maxSize = 0.0d;
		double maxFrequency = 0.0d;
		
		if (!caseSensitive) {
			for (int i = 0; i < search.length; i++) {
				search[i] = search[i].toLowerCase();
			}
		}
		
		HashSet<String> searchSet = new HashSet<String>(Arrays.asList(search));
		searchSet.remove(null);
		searchSet.remove("");
		
		if (caseSensitive) {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = levenshteinCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentDistance = result.get(i).getRight().getLeft();
						if (maxLevenshtein < currentDistance) {
							maxLevenshtein = currentDistance;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsRows.entrySet()) {
						String key = kvp.getKey();
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						if (currentDistance <= 3) {
							keyLevenshtein.add(new Int2Pair<String>(key, currentDistance));
							if (maxLevenshtein < currentDistance) {
								maxLevenshtein = currentDistance;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(levenshteinCache, s, key, currentDistance, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				int frequency = Collections.frequency(keyLevenshtein, keyLevenshtein.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = keyLevenshtein.get(i).getRight() / maxLevenshtein;
				score -= (containsRows.get(keyLevenshtein.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyLevenshtein.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				List<Pair<String, IntIntPair>> result = levenshteinCache.getIfPresent(s);
				if (result != null) {
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
						int currentDistance = result.get(i).getRight().getLeft();
						if (maxLevenshtein < currentDistance) {
							maxLevenshtein = currentDistance;
						}
						int size = result.get(i).getRight().getRight();
						if (maxSize < size) {
							maxSize = size;
						}
					}
				} else {
					for (Entry<String, IntList> kvp : containsCiRows.entrySet()) {
						String key = kvp.getKey();
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						if (currentDistance <= 3) {
							keyLevenshtein.add(new Int2Pair<String>(key, currentDistance));
							if (maxLevenshtein < currentDistance) {
								maxLevenshtein = currentDistance;
							}
							int size = kvp.getValue().size();
							if (maxSize < size) {
								maxSize = size;
							}
							
							addToCache(levenshteinCache, s, key, currentDistance, size);
						}
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				int frequency = Collections.frequency(keyLevenshtein, keyLevenshtein.get(i));
				if (maxFrequency < frequency) {
					maxFrequency = frequency;
				}
			}
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = keyLevenshtein.get(i).getRight() / maxLevenshtein;
				score -= (containsCiRows.get(keyLevenshtein.get(i).getLeft()).size() / maxSize) / 5.0d;
				score -= ((Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Double2Pair<String>(keyLevenshtein.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Double2Pair<String>>() {
			@Override
			public int compare(Double2Pair<String> one, Double2Pair<String> two) {
				return Double.compare(one.getRight(), two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				IntList temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.getInt(j))) {
						ids.add(temp.getInt(j));
					}
				}
			}
		}
		
		return ids.toIntArray();
	}
	
	/**
	 * Returns the row IDs of any double-metaphone matches and sorts the results by a relevance score descending and ID ascending. This search is always case-insensitive.
	 * 
	 * @param search The search query
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] doubleMetaphone(String search) {
		return doubleMetaphone(new String[] {search});
	}
	/**
	 * Returns the row IDs of any double-metaphone matches and sorts the results by a relevance score descending and ID ascending. This search is always case-insensitive.
	 * 
	 * @param search The search query
	 * @param delimiter An optional delimiter to split the string into several searches
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] doubleMetaphone(String search, char delimiter) {
		return doubleMetaphone(search.split("\\" + delimiter));
	}
	/**
	 * Returns the row IDs of any double-metaphone matches and sorts the results by a relevance score descending and ID ascending. This search is always case-insensitive.
	 * 
	 * @param search The search query
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] doubleMetaphone(String[] search) {
		IntList ids = new IntArrayList();
		ArrayList<Int2Pair<String>> keyLevenshtein = new ArrayList<Int2Pair<String>>();
		ArrayList<Double2Pair<String>> keyScore = new ArrayList<Double2Pair<String>>();
		double maxLevenshtein = 0.0d;
		double maxSize = 0.0d;
		double maxFrequency = 0.0d;
		
		String[] searchDm = new String[search.length * 2];
		for (int i = 0; i < search.length; i++) {
			searchDm[i * 2] = dm.doubleMetaphone(search[i], false);
			searchDm[i * 2 + 1] = dm.doubleMetaphone(search[i], true);
		}
		
		HashSet<String> searchSet = new HashSet<String>(Arrays.asList(searchDm));
		searchSet.remove(null);
		searchSet.remove("");
		
		for (String s : searchSet) {
			List<Pair<String, IntIntPair>> result = dmCache.getIfPresent(s);
			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					keyLevenshtein.add(new Int2Pair<String>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
					int currentDistance = result.get(i).getRight().getLeft();
					if (maxLevenshtein < currentDistance) {
						maxLevenshtein = currentDistance;
					}
					int size = result.get(i).getRight().getRight();
					if (maxSize < size) {
						maxSize = size;
					}
				}
			} else {
				for (Entry<String, IntList> kvp : containsDmRows.entrySet()) {
					String key = kvp.getKey();
					if (key.contains(s)) {
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						keyLevenshtein.add(new Int2Pair<String>(key, currentDistance));
						if (maxLevenshtein < currentDistance) {
							maxLevenshtein = currentDistance;
						}
						int size = kvp.getValue().size();
						if (maxSize < size) {
							maxSize = size;
						}
						
						addToCache(dmCache, s, key, currentDistance, size);
					}
				}
			}
		}
		
		for (int i = 0; i < keyLevenshtein.size(); i++) {
			int frequency = Collections.frequency(keyLevenshtein, keyLevenshtein.get(i));
			if (maxFrequency < frequency) {
				maxFrequency = frequency;
			}
		}
		for (int i = 0; i < keyLevenshtein.size(); i++) {
			double score = keyLevenshtein.get(i).getRight() / maxLevenshtein;
			score -= (containsDmRows.get(keyLevenshtein.get(i).getLeft()).size() / maxSize) / 5.0d;
			score -= ((Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
			keyScore.add(new Double2Pair<String>(keyLevenshtein.get(i).getLeft(), score));
		}
		
		keyScore.sort(new Comparator<Double2Pair<String>>() {
			@Override
			public int compare(Double2Pair<String> one, Double2Pair<String> two) {
				return Double.compare(one.getRight(), two.getRight());
			}
		});
		
		for (int i = 0; i < keyScore.size(); i++) {
			IntList temp = containsDmRows.get(keyScore.get(i).getLeft());
			for (int j = 0; j < temp.size(); j++) {
				if (!ids.contains(temp.getInt(j))) {
					ids.add(temp.getInt(j));
				}
			}
		}
		
		return ids.toIntArray();
	}
	/**
	 * Combines exact, substring, and double-metaphone searches and sorts the results by a relevance score descending and ID ascending, then returns those results.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] naturalLanguage(String search, boolean caseSensitive) {
		return naturalLanguage(new String[] {search}, caseSensitive);
	}
	/**
	 * Combines exact, substring, and double-metaphone searches and sorts the results by a relevance score descending and ID ascending, then returns those results.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @param delimiter An optional delimiter to split the string into several searches
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] naturalLanguage(String search, boolean caseSensitive, char delimiter) {
		return naturalLanguage(search.split("\\" + delimiter), caseSensitive);
	}
	/**
	 * Combines exact, substring, and double-metaphone searches and sorts the results by a relevance score descending and ID ascending, then returns those results.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public synchronized int[] naturalLanguage(String[] search, boolean caseSensitive) {
		IntList ids = new IntArrayList();
		
		int[] exactMatches = exact(search, caseSensitive);
		for (int i = 0; i < exactMatches.length; i++) {
			ids.add(exactMatches[i]);
		}
		
		int[] substringMatches = substring(search, caseSensitive);
		for (int i = 0; i < substringMatches.length; i++) {
			if (!ids.contains(substringMatches[i])) {
				ids.add(substringMatches[i]);
			}
		}
		
		int[] levenshteinMatches = levenshtein(search, caseSensitive);
		for (int i = 0; i < levenshteinMatches.length; i++) {
			if (!ids.contains(levenshteinMatches[i])) {
				ids.add(levenshteinMatches[i]);
			}
		}
		
		int[] metaphoneMatches = doubleMetaphone(search);
		for (int i = 0; i < metaphoneMatches.length; i++) {
			if (!ids.contains(metaphoneMatches[i])) {
				ids.add(metaphoneMatches[i]);
			}
		}
		
		return ids.toIntArray();
	}
	
	/**
	 * Gets the value of any cell retrieved by row and column index.
	 * 
	 * @param rowIndex The row ID
	 * @param columnIndex The index of the column to retrieve
	 * @return The value of the cell retrieved by row and column ID
	 */
	public synchronized String getValue(int rowIndex, int columnIndex) {
		if (columnIndex < 0 || rowIndex < 0 || rowIndex >= rows.size()) {
			return null;
		}
		List<String> row = rows.get(rowIndex);
		if (columnIndex >= row.size()) {
			return null;
		}
		
		return row.get(columnIndex);
	}
	/**
	 * Gets the values of any cells retrieved by multiple row indices and a single column index.
	 * 
	 * @param rowIndices The row IDs
	 * @param columnIndex The index of the column to retrieve
	 * @return The values of the cells retrieved by row IDs and column ID
	 */
	public synchronized String[] getValues(int[] rowIndices, int columnIndex) {
		if (rowIndices == null) {
			return null;
		}
		if (rowIndices.length == 0 || columnIndex < 0) {
			return new String[0];
		}
		
		ArrayList<String> values = new ArrayList<String>();
		for (int i = 0; i < rowIndices.length; i++) {
			if (rowIndices[i] >= rows.size()) {
				continue;
			}
			List<String> row = rows.get(rowIndices[i]);
			if (columnIndex >= row.size()) {
				continue;
			}
			values.add(row.get(columnIndex));
		}
		
		return values.toArray(new String[0]);
	}
	
	//private
	private List<String> generateDm(String[] input) {
		ArrayList<String> retVal = new ArrayList<String>();
		
		for (int i = 0; i < input.length; i++) {
			retVal.add(dm.doubleMetaphone(input[i], false) + ";" + dm.doubleMetaphone(input[i], true));
		}
		
		return retVal;
	}
	private void removeFromMap(Map<String, IntList> m, int index) {
		for (Entry<String, IntList> kvp : m.entrySet()) {
			IntList value = kvp.getValue();
			
			int ind = value.indexOf(index);
			if (ind > -1) {
				value.removeInt(ind);
				kvp.setValue(value);
			}
		}
	}
	
	private void addToCache(Cache<String, List<Pair<String, IntIntPair>>> cache, String key, String dbKey, int distance, int size) {
		List<Pair<String, IntIntPair>> value = cache.getIfPresent(key);
		if (value != null) {
			value.add(new Pair<String, IntIntPair>(dbKey, new IntIntPair(distance, size)));
		} else {
			cache.put(key, new ArrayList<Pair<String, IntIntPair>>(Arrays.asList(new Pair<String, IntIntPair>(dbKey, new IntIntPair(distance, size)))));
		}
	}
	
	private String[] stripBlanksAndDuplicates(String[] input) {
		LinkedHashSet<String> retVal = new LinkedHashSet<String>();
		
		for (int i = 0; i < input.length; i++) {
			if (input[i] == null || input[i].isEmpty()) {
				continue;
			}
			retVal.add(input[i]);
		}
		
		return retVal.toArray(new String[0]);
	}
}
