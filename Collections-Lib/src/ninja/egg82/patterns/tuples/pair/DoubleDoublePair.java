package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public final class DoubleDoublePair {
	//vars
	private volatile double left = 0.0d;
	private volatile double right = 0.0d;
	
	private volatile int hashCode = 0;
	
	//constructor
	public DoubleDoublePair(double left, double right) {
		this.left = left;
		this.right = right;
		
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//public
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
		this.left = left;
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	public double getRight() {
		return right;
	}
	public void setRight(double right) {
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
		if (!CollectionsReflectUtil.doesExtend(getClass(), obj.getClass())) {
			return false;
		}
		
		DoubleDoublePair p = (DoubleDoublePair) obj;
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
