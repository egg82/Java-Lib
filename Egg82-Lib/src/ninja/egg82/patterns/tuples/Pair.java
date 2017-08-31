package ninja.egg82.patterns.tuples;

import org.apache.commons.lang.builder.HashCodeBuilder;

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
	public void setLeft(L left) {
		this.left = left;
	}
	public R getRight() {
		return right;
	}
	public void setRight(R right) {
		this.right = right;
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
		
		Pair<?, ?> p = (Pair<?, ?>) obj;
		if (p.getLeft().equals(left) && p.getRight().equals(right)) {
			return true;
		}
		
		return false;
	}
	public int hashCode() {
		return new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//private
	
}
