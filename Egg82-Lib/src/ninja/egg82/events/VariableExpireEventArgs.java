package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class VariableExpireEventArgs<K> extends EventArgs {
	//vars
	public static final VariableExpireEventArgs<Object> EMPTY = new VariableExpireEventArgs<Object>(null, null, null);
	
	private K key = null;
	private Class<?> valueClass = null;
	private Object value = null;
	
	//constructor
	public VariableExpireEventArgs(K key, Class<?> valueClass, Object value) {
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
