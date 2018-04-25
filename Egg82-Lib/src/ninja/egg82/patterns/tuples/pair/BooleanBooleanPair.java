package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class BooleanBooleanPair {
	//vars
	private volatile boolean left = false;
	private volatile boolean right = false;
	
	private volatile int hashCode = 0;
	
	//constructor
	public BooleanBooleanPair(boolean left, boolean right) {
		this.left = left;
		this.right = right;
		
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//public
	public boolean getLeft() {
		return left;
	}
	public void setLeft(boolean left) {
		this.left = left;
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	public boolean getRight() {
		return right;
	}
	public void setRight(boolean right) {
		this.right = right;
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
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
		
		BooleanBooleanPair p = (BooleanBooleanPair) obj;
		if (
			(p.left == left)
			&& (p.right == right)
		) {
			return true;
		}
		
		return false;
	}
	public int hashCode() {
		return hashCode;
	}
	
	//private
	
}
