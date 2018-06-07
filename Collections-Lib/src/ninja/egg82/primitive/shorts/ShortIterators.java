package ninja.egg82.primitive.shorts;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ShortIterators {
	//vars
	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();
	
	//constructor
	public ShortIterators() {
		
	}
	
	//public
	public static ShortListIterator singleton(final short element) {
		return new SingletonIterator(element);
	}
	
	public static ShortListIterator wrap(final short[] array, final int offset, final int length) {
		ShortArrays.ensureOffsetLength(array, offset, length);
		return new ArrayIterator(array, offset, length);
	}
	public static ShortListIterator wrap(final short[] array) {
		return new ArrayIterator(array, 0, array.length);
	}
	
	public static int unwrap(final ShortIterator i, final short array[], int offset, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		if (offset < 0 || offset + max > array.length) {
			throw new IllegalArgumentException();
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			array[offset++] = i.nextShort();
		}
		return max - j - 1;
	}
	public static int unwrap(final ShortIterator i, final short array[]) {
		return unwrap(i, array, 0, array.length);
	}
	public static short[] unwrap(final ShortIterator i, int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		short array[] = new short[16];
		int j = 0;
		while (max-- != 0 && i.hasNext()) {
			if (j == array.length) {
				array = ShortArrays.grow(array, j + 1);
			}
			array[j++] = i.nextShort();
		}
		return ShortArrays.trim(array, j);
	}
	public static short[] unwrap(final ShortIterator i) {
		return unwrap(i, Integer.MAX_VALUE);
	}
	public static int unwrap(final ShortIterator i, final ShortCollection c, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			c.add(i.next());
		}
		return max - j - 1;
	}
	public static long unwrap(final ShortIterator i, final ShortCollection c) {
		long n = 0;
		while (i.hasNext()) {
			c.add(i.next());
			n++;
		}
		return n;
	}
	
	public static int pour(final ShortIterator i, final ShortCollection s, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			s.add(i.nextShort());
		}
		return max - j - 1;
	}
	public static int pour(final ShortIterator i, final ShortCollection s) {
		return pour(i, s, Integer.MAX_VALUE);
	}
	public static ShortList pour(final ShortIterator i, int max) {
		final ShortArrayList l = new ShortArrayList();
		pour(i, l, max);
		l.trim();
		return l;
	}
	public static ShortList pour(final ShortIterator i) {
		return pour(i, Integer.MAX_VALUE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ShortIterator asShortIterator(final Iterator i) {
		if (i instanceof ShortIterator) {
			return (ShortIterator) i;
		}
		return new IteratorWrapper(i);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ShortListIterator asShortIterator(final ListIterator i) {
		if (i instanceof ShortListIterator) {
			return (ShortListIterator) i;
		}
		return new ListIteratorWrapper(i);
	}
	
	public static class EmptyIterator extends AbstractShortListIterator implements Serializable, Cloneable {
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
		
		public short nextShort() {
			throw new NoSuchElementException();
		}
		public short previousShort() {
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
	private static class SingletonIterator extends AbstractShortListIterator {
		//vars
		private final short element;
		private int curr;
		
		//constructor
		public SingletonIterator(final short element) {
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
		
		public short nextShort() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			curr = 1;
			return element;
		}
		public short previousShort() {
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
	private static class ArrayIterator extends AbstractShortListIterator {
		//vars
		private final short[] array;
		private final int offset;
		private final int length;
		private int curr;
		
		//constructor
		public ArrayIterator(final short[] array, final int offset, final int length) {
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
		
		public short nextShort() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return array[offset + curr++];
		}
		public short previousShort() {
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
	private static class IteratorWrapper extends AbstractShortIterator {
		//vars
		final Iterator<Short> i;
		
		//constructor
		public IteratorWrapper(final Iterator<Short> i) {
			this.i = i;
		}
		
		//public
		public boolean hasNext() {
			return i.hasNext();
		}
		public void remove() {
			i.remove();
		}
		public short nextShort() {
			return i.next().shortValue();
		}
		
		//private
		
	}
	private static class ListIteratorWrapper extends AbstractShortListIterator {
		//vars
		final ListIterator<Short> i;
		
		//constructor
		public ListIteratorWrapper(final ListIterator<Short> i) {
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
		public void set(short k) {
			i.set(Short.valueOf(k));
		}
		public void add(short k) {
			i.add(Short.valueOf(k));
		}
		public void remove() {
			i.remove();
		}
		public short nextShort() {
			return i.next().shortValue();
		}
		public short previousShort() {
			return i.previous().shortValue();
		}
		
		//private
		
	}
}
