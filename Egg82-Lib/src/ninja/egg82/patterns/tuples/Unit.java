package ninja.egg82.patterns.tuples;

import ninja.egg82.utils.ReflectUtil;

public class Unit<T> {
	//vars
	private T type = null;
	
	//constructor
	public Unit(T type) {
		this.type = type;
	}
	
	//public
	public T getType() {
		return type;
	}
	public void setType(T type) {
		this.type = type;
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
		
		Unit<?> u = (Unit<?>) obj;
		if (u.getType().equals(type)) {
			return true;
		}
		
		return false;
	}
	
	//private
	
}
