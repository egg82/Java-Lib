package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class RegisterExpireEventArgs<K, V> extends EventArgs {
	//vars
	public static final RegisterExpireEventArgs<Object, Object> EMPTY = new RegisterExpireEventArgs<Object, Object>(null, null);
	
	private K key = null;
	private V value = null;
	
	//constructor
	public RegisterExpireEventArgs(K key, V value) {
		super();
		
		this.key = key;
		this.value = value;
	}
	
	//public
	public K getKey() {
		return key;
	}
	public V getValue() {
		return value;
	}
	
	//private
	
}
