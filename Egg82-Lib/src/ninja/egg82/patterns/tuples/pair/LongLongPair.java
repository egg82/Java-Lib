package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class LongLongPair {
	//vars
	private volatile long left = 0L;
	private volatile long right = 0L;
	
	//constructor
	public LongLongPair(long left, long right) {
		this.left = left;
		this.right = right;
	}
	
	//public
	public long getLeft() {
		return left;
	}
	public void setLeft(long left) {
		this.left = left;
	}
	public long getRight() {
		return right;
	}
	public void setRight(long right) {
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
		
		LongLongPair p = (LongLongPair) obj;
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
