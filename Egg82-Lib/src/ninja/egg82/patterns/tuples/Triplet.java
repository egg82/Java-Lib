package ninja.egg82.patterns.tuples;

import org.apache.commons.lang.builder.HashCodeBuilder;

import ninja.egg82.utils.ReflectUtil;

public class Triplet<L, C, R> {
	//vars
	private L left = null;
	private C center = null;
	private R right = null;
	
	//constructor
	public Triplet(L left, C center, R right) {
		this.left = left;
		this.center = center;
		this.right = right;
	}
	
	//public
	public L getLeft() {
		return left;
	}
	public void setLeft(L left) {
		this.left = left;
	}
	public C getCenter() {
		return center;
	}
	public void setCenter(C center) {
		this.center = center;
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
		
		Triplet<?, ?, ?> t = (Triplet<?, ?, ?>) obj;
		if (t.getLeft().equals(left) && t.getCenter().equals(center) && t.getRight().equals(right)) {
			return true;
		}
		
		return false;
	}
	public int hashCode() {
		return new HashCodeBuilder(938402693, 23487979).append(left).append(center).append(right).toHashCode();
	}
	
	//private
	
}
