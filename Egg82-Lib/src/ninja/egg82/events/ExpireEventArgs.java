package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class ExpireEventArgs<K, V> extends EventArgs {
	//vars
	public static final ExpireEventArgs<Object, Object> EMPTY = new ExpireEventArgs<Object, Object>(null, null);
	
	private K key = null;
	private V value = null;
	
	//constructor
	public ExpireEventArgs(K key, V value) {
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
