package ninja.egg82.patterns;

public interface IRegistry<K> {
	//functions
	void setRegister(K key, Object data);
	Object removeRegister(K key);
	<T> T removeRegister(K key, Class<T> type);
	
	Object getRegister(K key);
	<T> T getRegister(K key, Class<T> type);
	K getKey(Object data);
	Class<?> getRegisterClass(K key);
	
	Class<K> getKeyClass();
	
	boolean hasRegister(K key);
	boolean hasValue(Object data);
	
	void clear();
	K[] getKeys();
}
