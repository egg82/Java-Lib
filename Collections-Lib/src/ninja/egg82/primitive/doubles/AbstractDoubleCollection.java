package ninja.egg82.primitive.doubles;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectIterators;

public abstract class AbstractDoubleCollection extends AbstractCollection<Double> implements DoubleCollection {
	//vars
	
	//constructor
	protected AbstractDoubleCollection() {
		super();
	}
	
	//public
	public abstract DoubleIterator iterator();
	@Deprecated
	public DoubleIterator doubleIterator() {
		return iterator();
	}
	
	public double[] toArray(double a[]) {
		return toDoubleArray(a);
	}
	public double[] toDoubleArray() {
		return toDoubleArray(null);
	}
	public double[] toDoubleArray(double a[]) {
		if (a == null || a.length < size()) {
			a = new double[size()];
		}
		DoubleIterators.unwrap(iterator(), a);
		return a;
	}
	
	public boolean addAll(DoubleCollection c) {
		boolean retVal = false;
		final DoubleIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if (add(i.next())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean containsAll(DoubleCollection c) {
		final DoubleIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if(!contains(i.next())) {
				return false;
			}
		}
		
		return true;
	}
	public boolean retainAll(DoubleCollection c) {
		boolean retVal = false;
		int n = size();
		final DoubleIterator i = iterator();
		
		while (n-- != 0) {
			if (!c.contains(i.nextDouble())) {
				i.remove();
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean removeAll(DoubleCollection c) {
		boolean retVal = false;
		int n = c.size();
		final DoubleIterator i = c.iterator();
		
		while (n-- != 0) {
			if (rem(i.nextDouble())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean addAll(Collection<? extends Double> c) {
		boolean retVal = false;
		final Iterator<? extends Double> i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if (add(i.next())) {
				retVal = true;
			}
		}
		return retVal;
	}
	public boolean containsAll(Collection<?> c) {
		int n = c.size();
		final Iterator<?> i = c.iterator();
		
		while (n-- != 0) {
			if (!contains(i.next())) {
				return false;
			}
		}
		return true;
	}
	public boolean retainAll(Collection<?> c) {
		boolean retVal = false;
		int n = size();
		final Iterator<?> i = iterator();
		
		while (n-- != 0) {
			if (!c.contains(i.next())) {
				i.remove();
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean removeAll(Collection<?> c) {
		boolean retVal = false;
		int n = c.size();
		final Iterator<?> i = c.iterator();
		
		while (n-- != 0) {
			if (remove(i.next())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean add(double k) {
		throw new UnsupportedOperationException();
	}
	public boolean add(final Double o) {
		return add(o.doubleValue());
	}
	public boolean rem(final Object o) {
		return rem(((Double) o).doubleValue());
	}
	public boolean contains(final Object o) {
		return contains(((Double) o).doubleValue());
	}
	public boolean contains(final double k) {
		final DoubleIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextDouble()) {
				return true;
			}
		}
		return false;
	}
	public boolean rem(final double k) {
		final DoubleIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextDouble()) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public Object[] toArray() {
		final Object[] a = new Object[size()];
		ObjectIterators.unwrap(iterator(), a);
		return a;
	}
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (a.length < size()) {
			a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
		}
		ObjectIterators.unwrap(iterator(), a);
		return a;
	}
	
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final DoubleIterator i = iterator();
		int n = size();
		double k;
		boolean first = true;
		
		s.append("{");
		while (n-- != 0) {
			if (first) {
				first = false;
			} else {
				s.append(", ");
			}
			
			k = i.nextDouble();
			s.append(k);
		}
		s.append("}");
		
		return s.toString();
	}
	
	//private
	
}
