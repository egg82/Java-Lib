package ninja.egg82.patterns.tuples.pair;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public final class Double2Pair<L> {
	//vars
	private volatile L left = null;
	private volatile double right = 0.0d;
	
	private volatile int hashCode = 0;
	
	//constructor
	public Double2Pair(L left, double right) {
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
		if (!ReflectUtil.doesExtend(getClass(), obj.getClass())) {
			return false;
		}
		
		Double2Pair<?> p = (Double2Pair<?>) obj;
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
