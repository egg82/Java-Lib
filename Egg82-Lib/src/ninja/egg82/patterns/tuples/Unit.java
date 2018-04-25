package ninja.egg82.patterns.tuples;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public class Unit<T> {
	//vars
	private volatile T type = null;
	
	private volatile int hashCode = 0;
	
	//constructor
	public Unit(T type) {
		this.type = type;
		hashCode = new HashCodeBuilder(1938359, 1938301).append(type).toHashCode();
	}
	
	//public
	public T getType() {
		return type;
	}
	public void setType(T type) {
		this.type = type;
		hashCode = new HashCodeBuilder(1938359, 1938301).append(type).toHashCode();
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!ReflectUtil.doesExtend(getClass(), obj.getClass())) {
			return false;
		}
		
		final Object t = ((Unit<?>) obj).type;
		if (((t == null && type == null) || (t != null && t.equals(type)))) {
			return true;
		}
		
		return false;
	}
	public int hashCode() {
		return hashCode;
	}
	
	//private
	
}
