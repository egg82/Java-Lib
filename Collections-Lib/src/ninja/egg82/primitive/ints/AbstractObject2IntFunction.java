package ninja.egg82.primitive.ints;

import java.io.Serializable;

public abstract class AbstractObject2IntFunction<K> implements Object2IntFunction<K>, Serializable {
	//vars
	private static final long serialVersionUID = -4940583368468432370L;
	protected int defRetValue;
	
	//constructor
	protected AbstractObject2IntFunction() {
		
	}
	
	//public
	public void defaultReturnValue(final int rv) {
		defRetValue = rv;
	}
	public int defaultReturnValue() {
		return defRetValue;
	}
	
	public int put(K key, int value) {
		throw new UnsupportedOperationException();
	}
	public int removeInt(Object key) {
		throw new UnsupportedOperationException();
	}
	
	public Integer get(final Object ok) {
		final Object k = (ok);
		return containsKey(k) ? (Integer.valueOf(getInt(k))) : null;
	}
	public Integer put(final K ok, final Integer ov) {
		final K k = ok;
		final boolean containsKey = containsKey(k);
		final int v = put(k, ov.intValue());
		return containsKey ? (Integer.valueOf(v)) : null;
	}
	public Integer remove(final Object ok) {
		final Object k = ok;
		final boolean containsKey = containsKey(k);
		final int v = removeInt(k);
		return containsKey ? (Integer.valueOf(v)) : null;
	}
	
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	//private
	
}
