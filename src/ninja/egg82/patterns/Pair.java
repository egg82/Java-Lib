package ninja.egg82.patterns;

import ninja.egg82.utils.ReflectUtil;

public final class Pair<L, R> {
	//vars
	private L left = null;
	private R right = null;
	
	//constructor
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	//public
	public L getLeft() {
		return left;
	}
	public R getRight() {
		return right;
	}
	
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!ReflectUtil.doesExtend(this.getClass(), obj.getClass())) {
			return false;
		}
		
		Pair<?, ?> p = (Pair<?, ?>) obj;
		if (p.getRight() == right) {
			return true;
		}
		
		return false;
	}
	
	//private
	
}
