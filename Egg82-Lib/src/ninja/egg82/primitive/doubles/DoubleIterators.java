package ninja.egg82.primitive.doubles;

import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class DoubleIterators {
	//vars
	public static final EmptyIterator EMPTY_ITERATOR = new EmptyIterator();
	
	//constructor
	public DoubleIterators() {
		
	}
	
	//public
	public static DoubleListIterator singleton(final double element) {
		return new SingletonIterator(element);
	}
	
	public static DoubleListIterator wrap(final double[] array, final int offset, final int length) {
		DoubleArrays.ensureOffsetLength(array, offset, length);
		return new ArrayIterator(array, offset, length);
	}
	public static DoubleListIterator wrap(final double[] array) {
		return new ArrayIterator(array, 0, array.length);
	}
	
	public static int unwrap(final DoubleIterator i, final double array[], int offset, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		if (offset < 0 || offset + max > array.length) {
			throw new IllegalArgumentException();
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			array[offset++] = i.nextDouble();
		}
		return max - j - 1;
	}
	public static int unwrap(final DoubleIterator i, final double array[]) {
		return unwrap(i, array, 0, array.length);
	}
	public static double[] unwrap(final DoubleIterator i, int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		double array[] = new double[16];
		int j = 0;
		while (max-- != 0 && i.hasNext()) {
			if (j == array.length) {
				array = DoubleArrays.grow(array, j + 1);
			}
			array[j++] = i.nextDouble();
		}
		return DoubleArrays.trim(array, j);
	}
	public static double[] unwrap(final DoubleIterator i) {
		return unwrap(i, Integer.MAX_VALUE);
	}
	public static int unwrap(final DoubleIterator i, final DoubleCollection c, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			c.add(i.next());
		}
		return max - j - 1;
	}
	public static long unwrap(final DoubleIterator i, final DoubleCollection c) {
		long n = 0;
		while (i.hasNext()) {
			c.add(i.next());
			n++;
		}
		return n;
	}
	
	public static int pour(final DoubleIterator i, final DoubleCollection s, final int max) {
		if (max < 0) {
			throw new IllegalArgumentException("The maximum number of elements (" + max + ") is negative");
		}
		int j = max;
		while (j-- != 0 && i.hasNext()) {
			s.add(i.nextDouble());
		}
		return max - j - 1;
	}
	public static int pour(final DoubleIterator i, final DoubleCollection s) {
		return pour(i, s, Integer.MAX_VALUE);
	}
	public static DoubleList pour(final DoubleIterator i, int max) {
		final DoubleArrayList l = new DoubleArrayList();
		pour(i, l, max);
		l.trim();
		return l;
	}
	public static DoubleList pour(final DoubleIterator i) {
		return pour(i, Integer.MAX_VALUE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DoubleIterator asDoubleIterator(final Iterator i) {
		if (i instanceof DoubleIterator) {
			return (DoubleIterator) i;
		}
		return new IteratorWrapper(i);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DoubleListIterator asDoubleIterator(final ListIterator i) {
		if (i instanceof DoubleListIterator) {
			return (DoubleListIterator) i;
		}
		return new ListIteratorWrapper(i);
	}
	
	public static class EmptyIterator extends AbstractDoubleListIterator implements Serializable, Cloneable {
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
		
		public double nextDouble() {
			throw new NoSuchElementException();
		}
		public double previousDouble() {
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
	private static class SingletonIterator extends AbstractDoubleListIterator {
		//vars
		private final double element;
		private int curr;
		
		//constructor
		public SingletonIterator(final double element) {
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
		
		public double nextDouble() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			curr = 1;
			return element;
		}
		public double previousDouble() {
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
	private static class ArrayIterator extends AbstractDoubleListIterator {
		//vars
		private final double[] array;
		private final int offset;
		private final int length;
		private int curr;
		
		//constructor
		public ArrayIterator(final double[] array, final int offset, final int length) {
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
		
		public double nextDouble() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return array[offset + curr++];
		}
		public double previousDouble() {
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
	private static class IteratorWrapper extends AbstractDoubleIterator {
		//vars
		final Iterator<Double> i;
		
		//constructor
		public IteratorWrapper(final Iterator<Double> i) {
			this.i = i;
		}
		
		//public
		public boolean hasNext() {
			return i.hasNext();
		}
		public void remove() {
			i.remove();
		}
		public double nextDouble() {
			return i.next().doubleValue();
		}
		
		//private
		
	}
	private static class ListIteratorWrapper extends AbstractDoubleListIterator {
		//vars
		final ListIterator<Double> i;
		
		//constructor
		public ListIteratorWrapper(final ListIterator<Double> i) {
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
		public void set(double k) {
			i.set(Double.valueOf(k));
		}
		public void add(double k) {
			i.add(Double.valueOf(k));
		}
		public void remove() {
			i.remove();
		}
		public double nextDouble() {
			return i.next().doubleValue();
		}
		public double previousDouble() {
			return i.previous().doubleValue();
		}
		
		//private
		
	}
}
