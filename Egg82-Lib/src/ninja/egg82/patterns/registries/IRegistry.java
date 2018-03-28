package ninja.egg82.patterns.registries;

public interface IRegistry<K, V> {
	//functions
	void setRegister(K key, V data);
	V removeRegister(K key);
	
	V getRegister(K key);
	K getKey(V data);
	
	Class<K> getKeyClass();
	Class<V> getValueClass();
	
	boolean hasRegister(K key);
	boolean hasValue(V data);
	
	void clear();
	K[] getKeys();
}
