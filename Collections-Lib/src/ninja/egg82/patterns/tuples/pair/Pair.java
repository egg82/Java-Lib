package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public final class Pair<L, R> {
	//vars
	private volatile L left = null;
	private volatile R right = null;
	
	private volatile int hashCode = 0;
	
	//constructor
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
		
		hashCode = new HashCodeBuilder(13048583, 9832513).append(left).append(right).toHashCode();
	}
	
	//public
	public L getLeft() {
		return left;
	}
	public void setLeft(L left) {
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
		
		Pair<?, ?> p = (Pair<?, ?>) obj;
		final Object l = p.left;
		final Object r = p.right;
		if (
			((l == null && left == null) || (l != null && l.equals(left)))
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
