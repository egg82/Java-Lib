package ninja.egg82.primitive.chars;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class AbstractCharList extends AbstractCharCollection implements CharList, CharStack {
	//vars
	
	//constructor
	protected AbstractCharList() {
		super();
	}
	
	//public
	@Deprecated
	public CharListIterator charListIterator() {
		return listIterator();
	}
	@Deprecated
	public CharListIterator charListIterator(final int index) {
		return listIterator(index);
	}
	public CharListIterator iterator() {
		return listIterator();
	}
	public CharListIterator listIterator() {
		return listIterator(0);
	}
	public CharListIterator listIterator(final int index) {
		return new AbstractCharListIterator() {
			int pos = index, last = -1;
			public boolean hasNext() {
				return pos < AbstractCharList.this.size();
			}
			public boolean hasPrevious() {
				return pos > 0;
			}
			public char nextChar() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return AbstractCharList.this.getChar(last = pos++);
			}
			public char previousChar() {
				if (!hasPrevious()) {
					throw new NoSuchElementException();
				}
				return AbstractCharList.this.getChar(last = --pos);
			}
			public int nextIndex() {
				return pos;
			}
			public int previousIndex() {
				return pos - 1;
			}
			public void add(char k) {
				if (last == -1) {
					throw new IllegalStateException();
				}
				AbstractCharList.this.add(pos++, k);
				last = -1;
			}
			public void set(char k) {
				if (last == -1) {
					throw new IllegalStateException();
				}
				AbstractCharList.this.set(last, k);
			}
			public void remove() {
				if (last == -1) {
					throw new IllegalStateException();
				}
				AbstractCharList.this.removeChar( last );
				if (last < pos) {
					pos--;
				}
				last = -1;
			}
		};
	}
	
	public void add(final int index, final char k) {
		throw new UnsupportedOperationException();
	}
	public boolean add(final char k) {
		add(size(), k);
		return true;
	}
	public char removeChar(int i) {
		throw new UnsupportedOperationException();
	}
	public char set(final int index, final char k) {
		throw new UnsupportedOperationException();
	}
	
	public boolean addAll(int index, final Collection<? extends Character> c) {
		ensureIndex(index);
		int n = c.size();
		
		if (n == 0) {
			return false;
		}
		
		Iterator<? extends Character> i = c.iterator();
		while (n-- != 0) {
			add(index++, i.next());
		}
		return true;
	}
	public boolean addAll(final Collection<? extends Character> c) {
		return addAll(size(), c);
	}
	
	public boolean contains(final char k) {
		return indexOf(k) >= 0;
	}
	
	public int indexOf(final char k) {
		final CharListIterator i = listIterator();
		char e;
		while (i.hasNext()) {
			e = i.nextChar();
			if (k == e) {
				return i.previousIndex();
			}
		}
		return -1;
	}
	public int lastIndexOf(final char k) {
		CharListIterator i = listIterator( size() );
		char e;
		while (i.hasPrevious()) {
			e = i.previousChar();
			if (k == e) {
				return i.nextIndex();
			}
		}
		return -1;
	}
	
	public void size(final int size) {
		int i = size();
		if (size > i) {
			while (i++ < size) {
				add((char) 0);
			}
		} else {
			while (i-- != size) {
				remove(i);
			}
		}
	}
	
	public CharList subList(final int from, final int to) {
		ensureIndex(from);
		ensureIndex(to);
		if (from > to) {
			throw new IndexOutOfBoundsException( "Start index (" + from + ") is greater than end index (" + to + ")" );
		}
		
		return new CharSubList(this, from, to);
	}
	@Deprecated
	public CharList charSubList(final int from, final int to) {
		return subList(from, to);
	}
	
	public void removeElements(final int from, final int to) {
		ensureIndex(to);
		CharListIterator i = listIterator(from);
		int n = to - from;
		
		if (n < 0) {
			throw new IllegalArgumentException( "Start index (" + from + ") is greater than end index (" + to + ")" );
		}
		
		while (n-- != 0) {
			i.nextChar();
			i.remove();
		}
	}
	public void addElements(int index, final char a[], int offset, int length) {
		ensureIndex(index);
		if (offset < 0) {
			throw new ArrayIndexOutOfBoundsException( "Offset (" + offset + ") is negative" );
		}
		if (offset + length > a.length) {
			throw new ArrayIndexOutOfBoundsException( "End index (" + ( offset + length ) + ") is greater than array length (" + a.length + ")" );
		}
		while (length-- != 0) {
			add(index++, a[offset++]);
		}
	}
	public void addElements(final int index, final char a[]) {
		addElements(index, a, 0, a.length);
	}
	
	public void getElements(final int from, final char a[], int offset, int length) {
		CharListIterator i = listIterator(from);
		if (offset < 0) {
			throw new ArrayIndexOutOfBoundsException( "Offset (" + offset + ") is negative" );
		}
		if (offset + length > a.length) {
			throw new ArrayIndexOutOfBoundsException( "End index (" + ( offset + length ) + ") is greater than array length (" + a.length + ")" );
		}
		if (from + length > size()) {
			throw new IndexOutOfBoundsException( "End index (" + ( from + length ) + ") is greater than list size (" + size() + ")" );
		}
		while (length-- != 0) {
			a[offset++] = i.nextChar();
		}
	}
	
	public boolean equals(final Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof List)) {
			return false;
		}
		
		final List<?> l = (List<?>) o;
		int s = size();
		if (s != l.size()) {
			return false;
		}
		
		if (l instanceof CharList) {
			final CharListIterator i1 = listIterator();
			final CharListIterator i2 = ((CharList) l).listIterator();
			while (s-- != 0) {
				if (i1.nextChar() != i2.nextChar()) {
					return false;
				}
			}
			return true;
		}
		final ListIterator<?> i1 = listIterator();
		final ListIterator<?> i2 = l.listIterator();
		while (s-- != 0) {
			if (!valEquals(i1.next(), i2.next())) {
				return false;
			}
		}
		return true;
	 }
	public int compareTo(final List<? extends Character> l) {
		if (l == this) {
			return 0;
		}
		
		if (l instanceof CharList) {
			final CharListIterator i1 = listIterator();
			final CharListIterator i2 = ((CharList) l).listIterator();
			int r;
			char e1;
			char e2;
			while (i1.hasNext() && i2.hasNext()) {
				e1 = i1.nextChar();
				e2 = i2.nextChar();
				if ((r = (e1 < e2 ? -1 : (e1 == e2 ? 0 : 1))) != 0) {
					return r;
				}
			}
			return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0 );
		}
		ListIterator<? extends Character> i1 = listIterator();
		ListIterator<? extends Character> i2 = l.listIterator();
		int r;
		while (i1.hasNext() && i2.hasNext()) {
			if ((r = ((Comparable<? super Character>) i1.next()).compareTo(i2.next())) != 0) {
				return r;
			}
		}
		return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0 );
	}
	public int hashCode() {
		CharIterator i = iterator();
		int h = 1, s = size();
		while (s-- != 0) {
			char k = i.nextChar();
			h = 31 * Integer.hashCode(h) + Character.hashCode(k);
		}
		return h;
	}
	
	public void push(char o) {
		add(o);
	}
	public char popChar() {
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		return removeChar(size() - 1);
	}
	public char topChar() {
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		return getChar(size() - 1);
	}
	public char peekChar(int i) {
		return getChar(size() - 1 - i);
	}
	public boolean rem(char k) {
		int index = indexOf(k);
		if (index == -1) {
			return false;
		}
		removeChar(index);
		return true;
	}
	public boolean remove(final Object o) {
		return rem(((Character) (o)).charValue());
	}
	public boolean addAll(final int index, final CharCollection c) {
		return addAll(index, (Collection<? extends Character>) c);
	}
	public boolean addAll(final int index, final CharList l) {
		return addAll(index, (CharCollection) l);
	}
	public boolean addAll(final CharCollection c) {
		return addAll(size(), c);
	}
	public boolean addAll(final CharList l) {
		return addAll(size(), l);
	}
	public void add(final int index, final Character ok) {
		add(index, ok.charValue());
	}
	public Character set(final int index, final Character ok) {
		return Character.valueOf(set(index, ok.charValue()));
	}
	public Character get(final int index) {
		return Character.valueOf(getChar(index));
	}
	public int indexOf(final Object ok) {
		return indexOf(((Character) ok).charValue());
	}
	public int lastIndexOf(final Object ok) {
		return lastIndexOf(((Character) ok).charValue());
	}
	public Character remove(final int index) {
		return Character.valueOf(removeChar(index));
	}
	public void push(Character o) {
		push(o.charValue());
	}
	public Character pop() {
		return Character.valueOf(popChar());
	}
	public Character top() {
		return Character.valueOf(topChar());
	}
	public Character peek(int i) {
		return Character.valueOf(peekChar(i));
	}
	
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final CharIterator i = iterator();
		int n = size();
		char k;
		boolean first = true;
		
		s.append("[");
		while (n-- != 0) {
			if (first) {
				first = false;
			} else {
				s.append(", ");
			}
			
			k = i.nextChar();
			s.append(k);
		}
		s.append("]");
		
		return s.toString();
	}
	
	public static class CharSubList extends AbstractCharList implements Serializable {
		//vars
		private static final boolean ASSERTS = false;
		private static final long serialVersionUID = -7046029254386353129L;
		
		protected final CharList l;
		protected final int from;
		protected int to;
		
		//constructor
		public CharSubList(final CharList l, final int from, final int to) {
			this.l = l;
			this.from = from;
			this.to = to;
		}
		
		//public
		public boolean add(final char k) {
			l.add(to, k);
			to++;
			if (ASSERTS) {
				assertRange();
			}
			return true;
		}
		public void add(final int index, final char k) {
			ensureIndex(index);
			l.add(from + index, k);
			to++;
			if (ASSERTS) {
				assertRange();
			}
		}
		
		public boolean addAll(final int index, final Collection<? extends Character> c) {
			ensureIndex(index);
			to += c.size();
			if (ASSERTS) {
				boolean retVal = l.addAll(from + index, c);
				assertRange();
				return retVal;
			}
			return l.addAll(from + index, c);
		}
		
		public char getChar(int index) {
			ensureRestrictedIndex(index);
			return l.getChar(from + index);
		}
		public char removeChar(int index) {
			ensureRestrictedIndex(index);
			to--;
			return l.removeChar(from + index);
		}
		public char set(int index, char k) {
			ensureRestrictedIndex(index);
			return l.set(from + index, k);
		}
		
		public void clear() {
			removeElements(0, size());
			if (ASSERTS) {
				assertRange();
			}
		}
		public int size() {
			return to - from;
		}
		
		public void getElements(final int from, final char[] a, final int offset, final int length) {
			ensureIndex(from);
			if (from + length > size()) {
				throw new IndexOutOfBoundsException("End index (" + from + length + ") is greater than list size (" + size() + ")");
			}
			l.getElements(this.from + from, a, offset, length);
		}
		public void removeElements(final int from, final int to) {
			ensureIndex(from);
			ensureIndex(to);
			l.removeElements(this.from + from, this.from + to);
			this.to -= (to - from);
			if (ASSERTS) {
				assertRange();
			}
		}
		public void addElements(int index, final char a[], int offset, int length) {
			ensureIndex(index);
			l.addElements(this.from + index, a, offset, length);
			this.to += length;
			if (ASSERTS) {
				assertRange();
			}
		}
		
		public CharListIterator listIterator(final int index) {
			ensureIndex(index);
			return new AbstractCharListIterator() {
				int pos = index, last = -1;
				
				public boolean hasNext() {
					return pos < size();
				}
				public boolean hasPrevious() {
					return pos > 0;
				}
				public char nextChar() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					return l.getChar(from + (last = pos++));
				}
				public char previousChar() {
					if (!hasPrevious()) {
						throw new NoSuchElementException();
					}
					return l.getChar(from + (last = --pos));
				}
				public int nextIndex() {
					return pos;
				}
				public int previousIndex() {
					return pos - 1;
				}
				public void add(char k) {
					if ( last == -1 ) {
						throw new IllegalStateException();
					}
					CharSubList.this.add(pos++, k);
					last = -1;
					if (ASSERTS) {
						assertRange();
					}
				}
				public void set(char k) {
					if (last < pos) {
						pos--;
					}
					last = -1;
					if (ASSERTS) {
						assertRange();
					}
				}
			};
		}
		
		public CharList subList(final int from, final int to) {
			ensureIndex(from);
			ensureIndex(to);
			if (from > to) {
				throw new IllegalArgumentException( "Start index (" + from + ") is greater than end index (" + to + ")" );
			}
			return new CharSubList(this, from, to);
		}
		
		public boolean rem(char k) {
			int index = indexOf(k);
			if (index == -1) {
				return false;
			}
			to--;
			l.removeChar(from + index);
			if (ASSERTS) {
				assertRange();
			}
			return true;
		}
		public boolean remove(final Object o) {
			return rem(((Character) (o)).charValue());
		}
		
		public boolean addAll(final int index, final CharCollection c) {
			ensureIndex(index);
			to += c.size();
			if (ASSERTS) {
				boolean retVal = l.addAll(from + index, c);
				assertRange();
				return retVal;
			}
			return l.addAll(from + index, c);
		}
		public boolean addAll(final int index, final CharList l) {
			ensureIndex(index);
			to += l.size();
			if (ASSERTS) {
				boolean retVal = this.l.addAll(from + index, l);
				assertRange();
				return retVal;
			}
			return this.l.addAll(from + index, l);
		}
		
		//private
		private void assertRange() {
			if (ASSERTS) {
				assert from <= l.size();
				assert to <= l.size();
				assert to >= from;
			}
		}
	}
	
	//private
	private boolean valEquals( final Object a, final Object b ) {
		return a == null ? b == null : a.equals( b );
	}
	
	protected void ensureIndex(final int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		}
		if (index > size()) {
			throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + size() + ")");
		}
	}
	protected void ensureRestrictedIndex(final int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
		}
		if (index >= size()) {
			throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size() + ")");
		}
	}
}
