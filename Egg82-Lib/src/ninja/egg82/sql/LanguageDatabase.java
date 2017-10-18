package ninja.egg82.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang.StringUtils;

import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.tuples.Pair;

public class LanguageDatabase {
	//vars
	private DoubleMetaphone dm = new DoubleMetaphone();
	
	private ArrayList<List<String>> rows = new ArrayList<List<String>>(); // Plaintext table
	private ArrayList<List<String>> ciRows = new ArrayList<List<String>>(); // Case-insensitive table
	private ArrayList<List<String>> dmRows = new ArrayList<List<String>>(); // DM table
	private HashMap<String, List<Integer>> containsRows = new HashMap<String, List<Integer>>(); // Needed for fast exact & contains matches
	private HashMap<String, List<Integer>> containsCiRows = new HashMap<String, List<Integer>>(); // Case-insensitive version
	private HashMap<String, List<Integer>> containsDmRows = new HashMap<String, List<Integer>>(); // There may be multiple rows with the same values, hence the list
	
	private LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>> exactCache = new LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>>(); // Last 1000 single-word searches
	private LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>> containsCache = new LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>>(); // Last 1000 single-word searches
	private LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>> levenshteinCache = new LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>>(); // Last 1000 single-word searches
	private LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>> dmCache = new LinkedHashMap<String, List<Pair<String, Pair<Integer, Integer>>>>(); // Last 1000 single-word searches
	
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
			throw new ArgumentNullException("columns");
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
				List<Integer> values = containsRows.get(csString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsRows.put(csString, new ArrayList<Integer>(Arrays.asList(index)));
			}
			if (containsCiRows.containsKey(ciString)) {
				List<Integer> values = containsCiRows.get(ciString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsCiRows.put(ciString, new ArrayList<Integer>(Arrays.asList(index)));
			}
			if (containsDmRows.containsKey(dmString)) {
				List<Integer> values = containsDmRows.get(dmString);
				if (!values.contains(index)) {
					values.add(index);
					Collections.sort(values);
				}
			} else {
				containsDmRows.put(dmString, new ArrayList<Integer>(Arrays.asList(index)));
			}
		}
		
		exactCache.clear();
		containsCache.clear();
		levenshteinCache.clear();
		dmCache.clear();
		
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
		
		exactCache.clear();
		containsCache.clear();
		levenshteinCache.clear();
		dmCache.clear();
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
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyColumn = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
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
				if (exactCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = exactCache.get(s);
					bumpCache(exactCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyColumn.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsRows.entrySet()) {
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
							
							keyColumn.add(new Pair<String, Integer>(key, currentColumn));
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
				double score = ((double) keyColumn.get(i).getRight()) / maxColumn;
				score -= (((double) containsRows.get(keyColumn.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyColumn, keyColumn.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyColumn.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				if (exactCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = exactCache.get(s);
					bumpCache(exactCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyColumn.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsCiRows.entrySet()) {
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
							
							keyColumn.add(new Pair<String, Integer>(key, currentColumn));
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
				double score = ((double) keyColumn.get(i).getRight()) / maxColumn;
				score -= (((double) containsCiRows.get(keyColumn.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyColumn, keyColumn.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyColumn.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> one, Pair<String, Double> two) {
				return one.getRight().compareTo(two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		}
		
		return ids.stream().mapToInt(i -> i).toArray();
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
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyLevenshtein = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
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
				if (containsCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = containsCache.get(s);
					bumpCache(containsCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsRows.entrySet()) {
						String key = kvp.getKey();
						if (key.contains(s) || s.contains(key)) {
							int currentDistance = StringUtils.getLevenshteinDistance(key, s);
							keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
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
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				if (containsCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = containsCache.get(s);
					bumpCache(containsCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsCiRows.entrySet()) {
						String key = kvp.getKey();
						if (key.contains(s) || s.contains(key)) {
							int currentDistance = StringUtils.getLevenshteinDistance(key, s);
							keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
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
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsCiRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> one, Pair<String, Double> two) {
				return one.getRight().compareTo(two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		}
		
		return ids.stream().mapToInt(i -> i).toArray();
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
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyLevenshtein = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
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
				if (levenshteinCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = levenshteinCache.get(s);
					bumpCache(levenshteinCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsRows.entrySet()) {
						String key = kvp.getKey();
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						if (currentDistance <= 3) {
							keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
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
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
			}
		} else {
			for (String s : searchSet) {
				if (levenshteinCache.containsKey(s)) {
					List<Pair<String, Pair<Integer, Integer>>> result = levenshteinCache.get(s);
					bumpCache(levenshteinCache, s);
					for (int i = 0; i < result.size(); i++) {
						keyLevenshtein.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
					for (Entry<String, List<Integer>> kvp : containsCiRows.entrySet()) {
						String key = kvp.getKey();
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						if (currentDistance <= 3) {
							keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
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
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsCiRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
				score -= (((double) Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
			}
		}
		
		keyScore.sort(new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> one, Pair<String, Double> two) {
				return one.getRight().compareTo(two.getRight());
			}
		});
		
		if (caseSensitive) {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		} else {
			for (int i = 0; i < keyScore.size(); i++) {
				List<Integer> temp = containsCiRows.get(keyScore.get(i).getLeft());
				for (int j = 0; j < temp.size(); j++) {
					if (!ids.contains(temp.get(j))) {
						ids.add(temp.get(j));
					}
				}
			}
		}
		
		return ids.stream().mapToInt(i -> i).toArray();
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
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyLevenshtein = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
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
			if (dmCache.containsKey(s)) {
				List<Pair<String, Pair<Integer, Integer>>> result = dmCache.get(s);
				bumpCache(dmCache, s);
				for (int i = 0; i < result.size(); i++) {
					keyLevenshtein.add(new Pair<String, Integer>(result.get(i).getLeft(), result.get(i).getRight().getLeft()));
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
				for (Entry<String, List<Integer>> kvp : containsDmRows.entrySet()) {
					String key = kvp.getKey();
					if (key.contains(s)) {
						int currentDistance = StringUtils.getLevenshteinDistance(key, s);
						keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
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
			double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
			score -= (((double) containsDmRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
			score -= (((double) Collections.frequency(keyLevenshtein, keyLevenshtein.get(i)) - 1.0d) / maxFrequency) / 5.0d;
			keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
		}
		
		keyScore.sort(new Comparator<Pair<String, Double>>() {
			@Override
			public int compare(Pair<String, Double> one, Pair<String, Double> two) {
				return one.getRight().compareTo(two.getRight());
			}
		});
		
		for (int i = 0; i < keyScore.size(); i++) {
			List<Integer> temp = containsDmRows.get(keyScore.get(i).getLeft());
			for (int j = 0; j < temp.size(); j++) {
				if (!ids.contains(temp.get(j))) {
					ids.add(temp.get(j));
				}
			}
		}
		
		return ids.stream().mapToInt(i -> i).toArray();
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
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
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
		
		return ids.stream().mapToInt(i -> i).toArray();
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
	private void removeFromMap(Map<String, List<Integer>> m, int index) {
		for (Entry<String, List<Integer>> kvp : m.entrySet()) {
			List<Integer> value = kvp.getValue();
			
			int ind = value.indexOf(index);
			if (ind > -1) {
				value.remove(ind);
				kvp.setValue(value);
			}
		}
	}
	
	private void bumpCache(Map<String, List<Pair<String, Pair<Integer, Integer>>>> cache, String key) {
		List<Pair<String, Pair<Integer, Integer>>> value = cache.get(key);
		cache.remove(key);
		cache.put(key, value);
	}
	private void addToCache(Map<String, List<Pair<String, Pair<Integer, Integer>>>> cache, String key, String dbKey, Integer distance, Integer size) {
		if (cache.containsKey(key)) {
			List<Pair<String, Pair<Integer, Integer>>> value = cache.get(key);
			value.add(new Pair<String, Pair<Integer, Integer>>(dbKey, new Pair<Integer, Integer>(distance, size)));
		} else {
			if (cache.size() >= 1000) {
				cache.remove(cache.entrySet().iterator().next().getKey());
			}
			cache.put(key, new ArrayList<Pair<String, Pair<Integer, Integer>>>(Arrays.asList(new Pair<String, Pair<Integer, Integer>>(dbKey, new Pair<Integer, Integer>(distance, size)))));
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
