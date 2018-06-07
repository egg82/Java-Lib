package ninja.egg82.primitive.shorts;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectIterators;

public abstract class AbstractShortCollection extends AbstractCollection<Short> implements ShortCollection {
	//vars
	
	//constructor
	protected AbstractShortCollection() {
		super();
	}
	
	//public
	public abstract ShortIterator iterator();
	@Deprecated
	public ShortIterator shortIterator() {
		return iterator();
	}
	
	public short[] toArray(short a[]) {
		return toShortArray(a);
	}
	public short[] toShortArray() {
		return toShortArray(null);
	}
	public short[] toShortArray(short a[]) {
		if (a == null || a.length < size()) {
			a = new short[size()];
		}
		ShortIterators.unwrap(iterator(), a);
		return a;
	}
	
	public boolean addAll(ShortCollection c) {
		boolean retVal = false;
		final ShortIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if (add(i.next())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean containsAll(ShortCollection c) {
		final ShortIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if(!contains(i.next())) {
				return false;
			}
		}
		
		return true;
	}
	public boolean retainAll(ShortCollection c) {
		boolean retVal = false;
		int n = size();
		final ShortIterator i = iterator();
		
		while (n-- != 0) {
			if (!c.contains(i.nextShort())) {
				i.remove();
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean removeAll(ShortCollection c) {
		boolean retVal = false;
		int n = c.size();
		final ShortIterator i = c.iterator();
		
		while (n-- != 0) {
			if (rem(i.nextShort())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean addAll(Collection<? extends Short> c) {
		boolean retVal = false;
		final Iterator<? extends Short> i = c.iterator();
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
	
	public boolean add(short k) {
		throw new UnsupportedOperationException();
	}
	public boolean add(final Short o) {
		return add(o.shortValue());
	}
	public boolean rem(final Object o) {
		return rem(((Short) o).shortValue());
	}
	public boolean contains(final Object o) {
		return contains(((Short) o).shortValue());
	}
	public boolean contains(final short k) {
		final ShortIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextShort()) {
				return true;
			}
		}
		return false;
	}
	public boolean rem(final short k) {
		final ShortIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextShort()) {
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
		final ShortIterator i = iterator();
		int n = size();
		short k;
		boolean first = true;
		
		s.append("{");
		while (n-- != 0) {
			if (first) {
				first = false;
			} else {
				s.append(", ");
			}
			
			k = i.nextShort();
			s.append(k);
		}
		s.append("}");
		
		return s.toString();
	}
	
	//private
	
}
