package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public final class Int2Pair<L> {
	//vars
	private volatile L left = null;
	private volatile int right = 0;
	
	private volatile int hashCode = 0;
	
	//constructor
	public Int2Pair(L left, int right) {
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
	public int getRight() {
		return right;
	}
	public void setRight(int right) {
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
		
		Int2Pair<?> p = (Int2Pair<?>) obj;
		final Object l = p.left;
		if (
			((l == null && left == null) || (l != null && l.equals(left)))
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
