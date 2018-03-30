package ninja.egg82.primitive.chars;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import it.unimi.dsi.fastutil.objects.ObjectIterators;

public abstract class AbstractCharCollection extends AbstractCollection<Character> implements CharCollection {
	//vars
	
	//constructor
	protected AbstractCharCollection() {
		super();
	}
	
	//public
	public abstract CharIterator iterator();
	@Deprecated
	public CharIterator charIterator() {
		return iterator();
	}
	
	public char[] toArray(char a[]) {
		return toCharArray(a);
	}
	public char[] toCharArray() {
		return toCharArray(null);
	}
	public char[] toCharArray(char a[]) {
		if (a == null || a.length < size()) {
			a = new char[size()];
		}
		CharIterators.unwrap(iterator(), a);
		return a;
	}
	
	public boolean addAll(CharCollection c) {
		boolean retVal = false;
		final CharIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if (add(i.next())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean containsAll(CharCollection c) {
		final CharIterator i = c.iterator();
		int n = c.size();
		
		while (n-- != 0) {
			if(!contains(i.next())) {
				return false;
			}
		}
		
		return true;
	}
	public boolean retainAll(CharCollection c) {
		boolean retVal = false;
		int n = size();
		final CharIterator i = iterator();
		
		while (n-- != 0) {
			if (!c.contains(i.nextChar())) {
				i.remove();
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean removeAll(CharCollection c) {
		boolean retVal = false;
		int n = c.size();
		final CharIterator i = c.iterator();
		
		while (n-- != 0) {
			if (rem(i.nextChar())) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	public boolean addAll(Collection<? extends Character> c) {
		boolean retVal = false;
		final Iterator<? extends Character> i = c.iterator();
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
	
	public boolean add(char k) {
		throw new UnsupportedOperationException();
	}
	public boolean add(final Character o) {
		return add(o.charValue());
	}
	public boolean rem(final Object o) {
		return rem(((((Character) (o)).charValue())));
	}
	public boolean contains(final Object o) {
		return contains(((((Character) (o)).charValue())));
	}
	public boolean contains(final char k) {
		final CharIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextChar()) {
				return true;
			}
		}
		return false;
	}
	public boolean rem(final char k) {
		final CharIterator iterator = iterator();
		while (iterator.hasNext()) {
			if (k == iterator.nextChar()) {
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
		final CharIterator i = iterator();
		int n = size();
		char k;
		boolean first = true;
		
		s.append("{");
		while (n-- != 0) {
			if (first) {
				first = false;
			} else {
				s.append(", ");
			}
			
			k = i.nextChar();
			s.append(k);
		}
		s.append("}");
		
		return s.toString();
	}
	
	//private
	
}
