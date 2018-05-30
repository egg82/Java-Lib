package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public final class FloatPair<R> {
	//vars
	private volatile float left = 0.0f;
	private volatile R right = null;
	
	private volatile int hashCode = 0;
	
	//constructor
	public FloatPair(float left, R right) {
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
	public R getRight() {
		return right;
	}
	public void setRight(R right) {
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
		
		FloatPair<?> p = (FloatPair<?>) obj;
		final Object r = p.right;
		if (
			(p.left == left)
			&& ((r == null && right == null) || (r != null && r.equals(right)))
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
