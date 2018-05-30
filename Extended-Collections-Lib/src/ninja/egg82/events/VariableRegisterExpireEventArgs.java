package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class VariableRegisterExpireEventArgs<K> extends EventArgs {
	//vars
	public static final VariableRegisterExpireEventArgs<Object> EMPTY = new VariableRegisterExpireEventArgs<Object>(null, null, null);
	
	private K key = null;
	private Class<?> valueClass = null;
	private Object value = null;
	
	//constructor
	public VariableRegisterExpireEventArgs(K key, Class<?> valueClass, Object value) {
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
