package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public final class FloatFloatPair {
	//vars
	private volatile float left = 0.0f;
	private volatile float right = 0.0f;
	
	private volatile int hashCode = 0;
	
	//constructor
	public FloatFloatPair(float left, float right) {
		this.left = left;
		this.right = right;
		
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//public
	public float getLeft() {
		return left;
	}
	public void setLeft(float left) {
		this.left = left;
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	public float getRight() {
		return right;
	}
	public void setRight(float right) {
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
		
		FloatFloatPair p = (FloatFloatPair) obj;
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
