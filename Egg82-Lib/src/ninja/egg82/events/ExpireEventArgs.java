package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class ExpireEventArgs<K> extends EventArgs {
	//vars
	public static final ExpireEventArgs<Object> EMPTY = new ExpireEventArgs<Object>(null, null, null);
	
	private K key = null;
	private Class<?> valueClass = null;
	private Object value = null;
	
	//constructor
	public ExpireEventArgs(K key, Class<?> valueClass, Object value) {
		super();
		
		this.key = key;
		this.valueClass = valueClass;
		this.value = value;
	}
	
	//public
	public K getKey() {
		return key;
	}
	public Class<?> getValueClass() {
		return valueClass;
	}
	public Object getValue() {
		return value;
	}
	
	//private
	
}
