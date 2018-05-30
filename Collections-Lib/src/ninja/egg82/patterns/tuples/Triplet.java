package ninja.egg82.patterns.tuples;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.core.CollectionsReflectUtil;

public class Triplet<L, C, R> {
	//vars
	private volatile L left = null;
	private volatile C center = null;
	private volatile R right = null;
	
	private volatile int hashCode = 0;
	
	//constructor
	public Triplet(L left, C center, R right) {
		this.left = left;
		this.center = center;
		this.right = right;
		
		hashCode = new HashCodeBuilder(938402693, 23487979).append(left).append(center).append(right).toHashCode();
	}
	
	//public
	public L getLeft() {
		return left;
	}
	public void setLeft(L left) {
		this.left = left;
		hashCode = new HashCodeBuilder(938402693, 23487979).append(left).append(center).append(right).toHashCode();
	}
	public C getCenter() {
		return center;
	}
	public void setCenter(C center) {
		this.center = center;
		hashCode = new HashCodeBuilder(938402693, 23487979).append(left).append(center).append(right).toHashCode();
	}
	public R getRight() {
		return right;
	}
	public void setRight(R right) {
		this.right = right;
		hashCode = new HashCodeBuilder(938402693, 23487979).append(left).append(center).append(right).toHashCode();
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
		
		Triplet<?, ?, ?> t = (Triplet<?, ?, ?>) obj;
		final Object l = t.left;
		final Object c = t.center;
		final Object r = t.right;
		if (
			((l == null && left == null) || (l != null && l.equals(left)))
			&& ((c == null && center == null) || (c != null && c.equals(center)))
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
