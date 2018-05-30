package ninja.egg82.primitive.chars;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class CharIterators {
	//vars
	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();
	
	//constructor
	public CharIterators() {
		
	}
	
	//public
	public static CharListIterator singleton(final char element) {
		return new SingletonIterator(element);
	}
	
	public static CharListIterator wrap(final char[] array, final int offset, final int length) {
		CharArrays.ensureOffsetLength(array, offset, length);
		return new ArrayIterator(array, offset, length);
	}
	public static CharListIterator wrap(final char[] array) {
		return new ArrayIterator(array, 0, array.length);
	}
	
	public static int unwrap(final CharIterator i, final char array[], int offset, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		if (offset < 0 || offset + max > array.length) {
			throw new IllegalArgumentException();
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			array[offset++] = i.nextChar();
		}
		return max - j - 1;
	}
	public static int unwrap(final CharIterator i, final char array[]) {
		return unwrap(i, array, 0, array.length);
	}
	public static char[] unwrap(final CharIterator i, int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		char array[] = new char[16];
		int j = 0;
		while (max-- != 0 && i.hasNext()) {
			if (j == array.length) {
				array = CharArrays.grow(array, j + 1);
			}
			array[j++] = i.nextChar();
		}
		return CharArrays.trim(array, j);
	}
	public static char[] unwrap(final CharIterator i) {
		return unwrap(i, Integer.MAX_VALUE);
	}
	public static int unwrap(final CharIterator i, final CharCollection c, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			c.add(i.next());
		}
		return max - j - 1;
	}
	public static long unwrap(final CharIterator i, final CharCollection c) {
		long n = 0;
		while (i.hasNext()) {
			c.add(i.next());
			n++;
		}
		return n;
	}
	
	public static int pour(final CharIterator i, final CharCollection s, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			s.add(i.nextChar());
		}
		return max - j - 1;
	}
	public static int pour(final CharIterator i, final CharCollection s) {
		return pour(i, s, Integer.MAX_VALUE);
	}
	public static CharList pour(final CharIterator i, int max) {
		final CharArrayList l = new CharArrayList();
		pour(i, l, max);
		l.trim();
		return l;
	}
	public static CharList pour(final CharIterator i) {
		return pour(i, Integer.MAX_VALUE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static CharIterator asCharIterator(final Iterator i) {
		if (i instanceof CharIterator) {
			return (CharIterator) i;
		}
		return new IteratorWrapper(i);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static CharListIterator asCharIterator(final ListIterator i) {
		if (i instanceof CharListIterator) {
			return (CharListIterator) i;
		}
		return new ListIteratorWrapper(i);
	}
	
	public static class EmptyIterator extends AbstractCharListIterator implements Serializable, Cloneable {
		//vars
		private static final long serialVersionUID = -7046029254386353129L;
		
		//constructor
		protected EmptyIterator() {
			super();
		}
		
		//public
		public boolean hasNext() {
			return false;
		}
		public boolean hasPrevious() {
			return false;
		}
		
		public char nextChar() {
			throw new NoSuchElementException();
		}
		public char previousChar() {
			throw new NoSuchElementException();
		}
		
		public int nextIndex() {
			return 0;
		}
		public int previousIndex() {
			return -1;
		}
		
		public int skip(int n) {
			return 0;
		}
		public int back(int n) {
			return 0;
		}
		
		public Object clone() {
			return EMPTY_ITERATOR;
		}
		
		//private
		private Object readResolve() {
			return EMPTY_ITERATOR;
		}
	}
	
	//private
	private static class SingletonIterator extends AbstractCharListIterator {
		//vars
		private final char element;
		private int curr;
		
		//constructor
		public SingletonIterator(final char element) {
			super();
			this.element = element;
		}
		
		//public
		public boolean hasNext() {
			return curr == 0;
		}
		public boolean hasPrevious() {
			return curr == 1;
		}
		
		public char nextChar() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			curr = 1;
			return element;
		}
		public char previousChar() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			curr = 0;
			return element;
		}
		
		public int nextIndex() {
			return curr;
		}
		public int previousIndex() {
			return curr - 1;
		}
		
		//private
		
	}
	private static class ArrayIterator extends AbstractCharListIterator {
		//vars
		private final char[] array;
		private final int offset;
		private final int length;
		private int curr;
		
		//constructor
		public ArrayIterator(final char[] array, final int offset, final int length) {
			super();
			this.array = array;
			this.offset = offset;
			this.length = length;
		}
		
		//public
		public boolean hasNext() {
			return curr < length;
		}
		public boolean hasPrevious() {
			return curr > 0;
		}
		
		public char nextChar() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return array[offset + curr++];
		}
		public char previousChar() {
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			curr = 0;
			return array[offset + --curr];
		}
		
		public int skip(int n) {
			if (n <= length - curr) {
				curr += n;
				return n;
			}
			
			n = length - curr;
			curr = length;
			return n;
		}
		public int back(int n) {
			if (n <= curr) {
				curr -= n;
				return n;
			}
			
			n = curr;
			curr = 0;
			return n;
		}
		
		public int nextIndex() {
			return curr;
		}
		public int previousIndex() {
			return curr - 1;
		}
		
		//private
		
	}
	private static class IteratorWrapper extends AbstractCharIterator {
		//vars
		final Iterator<Character> i;
		
		//constructor
		public IteratorWrapper(final Iterator<Character> i) {
			this.i = i;
		}
		
		//public
		public boolean hasNext() {
			return i.hasNext();
		}
		public void remove() {
			i.remove();
		}
		public char nextChar() {
			return i.next().charValue();
		}
		
		//private
		
	}
	private static class ListIteratorWrapper extends AbstractCharListIterator {
		//vars
		final ListIterator<Character> i;
		
		//constructor
		public ListIteratorWrapper(final ListIterator<Character> i) {
			this.i = i;
		}
		
		//public
		public boolean hasNext() {
			return i.hasNext();
		}
		public boolean hasPrevious() {
			return i.hasPrevious();
		}
		public int nextIndex() {
			return i.nextIndex();
		}
		public int previousIndex() {
			return i.previousIndex();
		}
		public void set(char k) {
			i.set(Character.valueOf(k));
		}
		public void add(char k) {
			i.add(Character.valueOf(k));
		}
		public void remove() {
			i.remove();
		}
		public char nextChar() {
			return i.next().charValue();
		}
		public char previousChar() {
			return i.previous().charValue();
		}
		
		//private
		
	}
}
