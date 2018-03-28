package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class DoublePair<R> {
	//vars
	private volatile double left = 0.0d;
	private volatile R right = null;
	
	//constructor
	public DoublePair(double left, R right) {
		this.left = left;
		this.right = right;
	}
	
	//public
	public double getLeft() {
		return left;
	}
	public void setLeft(double left) {
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
		
		DoublePair<?> p = (DoublePair<?>) obj;
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
		return new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//private
	
}
