package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class IntPair<R> {
	//vars
	private volatile int left = 0;
	private volatile R right = null;
	
	//constructor
	public IntPair(int left, R right) {
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
		
		IntPair<?> p = (IntPair<?>) obj;
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
