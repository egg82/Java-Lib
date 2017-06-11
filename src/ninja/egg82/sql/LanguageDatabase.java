package ninja.egg82.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.lang3.StringUtils;

import ninja.egg82.patterns.Pair;

public class LanguageDatabase {
	//vars
	private DoubleMetaphone dm = new DoubleMetaphone();
	
	private ArrayList<List<String>> rows = new ArrayList<List<String>>(); // Plaintext table
	private ArrayList<List<String>> ciRows = new ArrayList<List<String>>(); // Case-insensitive table
	private ArrayList<List<String>> dmRows = new ArrayList<List<String>>(); // DM table
	private HashMap<String, List<Integer>> containsRows = new HashMap<String, List<Integer>>(); // Needed for fast exact & contains matches
	private HashMap<String, List<Integer>> containsCiRows = new HashMap<String, List<Integer>>(); // Case-insensitive version
	private HashMap<String, List<Integer>> containsDmRows = new HashMap<String, List<Integer>>(); // There may be multiple rows with the same values, hence the list
	
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
	public int addRow(String... columns) {
		if (columns == null) {
			throw new RuntimeException("columns cannot be null");
		}
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
		
		return index;
	}
	/**
	 * Removes a row by the given index.
	 * 
	 * @param rowIndex The row ID to retrieve
	 */
	public void removeRow(int rowIndex) {
		rows.remove(rowIndex);
		ciRows.remove(rowIndex);
		dmRows.remove(rowIndex);
		
		removeFromMap(containsRows, rowIndex);
		removeFromMap(containsCiRows, rowIndex);
		removeFromMap(containsDmRows, rowIndex);
	}
	/**
	 * Returns the number of rows currently in the database.
	 * 
	 * @return The number of rows currently in the database
	 */
	public int numRows() {
		return rows.size();
	}
	/**
	 * Returns the number of columns for a given row in the database.
	 * 
	 * @param rowIndex The row ID
	 * @return The number of columns in the specified row ID
	 */
	public int numColumns(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			return -1;
		}
		return rows.get(rowIndex).size();
	}
	
	/**
	 * Returns the row IDs of any exact string matches.
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by ID ascending
	 */
	public int[] exact(String search, boolean caseSensitive) {
		List<Integer> ids = null;
		
		if (caseSensitive) {
			ids = containsRows.get(search);
		} else {
			search = search.toLowerCase();
			ids = containsCiRows.get(search);
		}
		
		return (ids != null) ? ids.stream().mapToInt(i -> i).toArray() : new int[0];
	}
	/**
	 * Returns the row IDs of any substring matches and sorts the results by a relevance score descending and ID ascending
	 * 
	 * @param search The search query
	 * @param caseSensitive Whether or not the search is case-sensitive
	 * @return A list of row IDs that contain the specified query, ordered by relevance score descending and then ID ascending
	 */
	public int[] substring(String search, boolean caseSensitive) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyLevenshtein = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
		double maxLevenshtein = 0.0d;
		double maxSize = 0.0d;
		
		if (caseSensitive) {
			for (Entry<String, List<Integer>> kvp : containsRows.entrySet()) {
				String key = kvp.getKey();
				if (key.contains(search)) {
					int currentDistance = StringUtils.getLevenshteinDistance(key, search);
					keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
					if (maxLevenshtein < currentDistance) {
						maxLevenshtein = currentDistance;
					}
					int size = kvp.getValue().size();
					if (maxSize < size) {
						maxSize = size;
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
				keyScore.add(new Pair<String, Double>(keyLevenshtein.get(i).getLeft(), score));
			}
		} else {
			search = search.toLowerCase();
			
			for (Entry<String, List<Integer>> kvp : containsCiRows.entrySet()) {
				String key = kvp.getKey();
				if (key.contains(search)) {
					int currentDistance = StringUtils.getLevenshteinDistance(key, search);
					keyLevenshtein.add(new Pair<String, Integer>(key, currentDistance));
					if (maxLevenshtein < currentDistance) {
						maxLevenshtein = currentDistance;
					}
					int size = kvp.getValue().size();
					if (maxSize < size) {
						maxSize = size;
					}
				}
			}
			
			for (int i = 0; i < keyLevenshtein.size(); i++) {
				double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
				score -= (((double) containsCiRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
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
	public int[] doubleMetaphone(String search) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ArrayList<Pair<String, Integer>> keyLevenshtein = new ArrayList<Pair<String, Integer>>();
		ArrayList<Pair<String, Double>> keyScore = new ArrayList<Pair<String, Double>>();
		double maxLevenshtein = 0.0d;
		double maxSize = 0.0d;
		
		String search1 = dm.doubleMetaphone(search, false);
		String search2 = dm.doubleMetaphone(search, true);
		
		for (Entry<String, List<Integer>> kvp : containsDmRows.entrySet()) {
			String key = kvp.getKey();
			if (key.contains(search1)) {
				int levenshtein = StringUtils.getLevenshteinDistance(key, search1);
				if (key.contains(search2)) {
					int tempLevenshtein = StringUtils.getLevenshteinDistance(key, search2);
					if (tempLevenshtein < levenshtein) {
						levenshtein = tempLevenshtein;
					}
				}
				keyLevenshtein.add(new Pair<String, Integer>(key, levenshtein));
				if (maxLevenshtein < levenshtein) {
					maxLevenshtein = levenshtein;
				}
				int size = kvp.getValue().size();
				if (maxSize < size) {
					maxSize = size;
				}
			} else if (key.contains(search2)) {
				int levenshtein = StringUtils.getLevenshteinDistance(key, search2);
				keyLevenshtein.add(new Pair<String, Integer>(key, levenshtein));
				if (maxLevenshtein < levenshtein) {
					maxLevenshtein = levenshtein;
				}
				int size = kvp.getValue().size();
				if (maxSize < size) {
					maxSize = size;
				}
			}
		}
		
		for (int i = 0; i < keyLevenshtein.size(); i++) {
			double score = ((double) keyLevenshtein.get(i).getRight()) / maxLevenshtein;
			score -= (((double) containsDmRows.get(keyLevenshtein.get(i).getLeft()).size()) / maxSize) / 5.0d;
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
	public int[] naturalLanguage(String search, boolean caseSensitive) {
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
	public String getValue(int rowIndex, int columnIndex) {
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
	public String[] getValues(int[] rowIndices, int columnIndex) {
		if (rowIndices.length == 0 || columnIndex < 0) {
			return null;
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
}
