package ninja.egg82.utils;

import java.util.Map;

public class CollectionUtil {
	//vars
	
	//constructor
	public CollectionUtil() {
		
	}
	
	//public
	/**
	 * Same as {@link Map#putIfAbsent(Object, Object)} except that if the old
	 * returned value is null, it returns the new value instead.
	 * 
	 * @param map The map to use
	 * @param key The key to modify
	 * @param newValue The new value to attempt to insert
	 * @return The old value; or, if null, the new value.
	 */
	public static <K, V> V putIfAbsent(Map<K, V> map, K key, V newValue) {
		final V oldValue = map.putIfAbsent(key, newValue);
		return (oldValue != null) ? oldValue : newValue;
	}
	
	//private
	
}
