package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class IntIntPair {
	//vars
	private volatile int left = 0;
	private volatile int right = 0;
	
	//constructor
	public IntIntPair(int left, int right) {
		this.left = left;
		this.right = right;
	}
	
	//public
	public int getLeft() {
		return left;
	}
	public void setLeft(int left) {
		this.left = left;
	}
	public int getRight() {
		return right;
	}
	public void setRight(int right) {
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
		
		IntIntPair p = (IntIntPair) obj;
		if (
			(p.left == left)
			&& (p.right == right)
		) {
			return true;
		}
		
		return false;
	}
	public int hashCode() {
		return new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//private
	
}
