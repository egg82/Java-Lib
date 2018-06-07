package ninja.egg82.primitive.doubles;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import it.unimi.dsi.fastutil.Arrays;

public class DoubleArrayList extends AbstractDoubleList implements RandomAccess, Cloneable, Serializable {
	//vars
	private static final long serialVersionUID = -7046029254386353130L;
	public final static int DEFAULT_INITIAL_CAPACITY = 16;
	private static final boolean ASSERTS = false;
	
	protected transient double a[];
	protected int size;
	
	//constructor
	@SuppressWarnings("unused")
	protected DoubleArrayList(final double a[], boolean dummy) {
		this.a = a;
	}
	public DoubleArrayList(final int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException( "Initial capacity (" + capacity + ") is negative" );
		}
		a = new double[capacity];
	}
	public DoubleArrayList() {
		this(DEFAULT_INITIAL_CAPACITY);
	}
	public DoubleArrayList(final Collection<? extends Double> c) {
		this(c.size());
		size = DoubleIterators.unwrap(DoubleIterators.asDoubleIterator(c.iterator()), a);
	}
	public DoubleArrayList(final DoubleCollection c) {
		this(c.size());
		size = DoubleIterators.unwrap(c.iterator(), a);
	}
	public DoubleArrayList(final DoubleList l) {
		this(l.size());
		l.getElements(0, a, 0, size = l.size());
	}
	public DoubleArrayList(final double a[]) {
		this(a, 0, a.length);
	}
	public DoubleArrayList(final double a[], final int offset, final int length) {
		this(length);
		System.arraycopy(a, offset, this.a, 0, length);
		size = length;
	}
	public DoubleArrayList(final Iterator<? extends Double> i) {
		this();
		while (i.hasNext()) {
			this.add(i.next());
		}
	}
	public DoubleArrayList(final DoubleIterator i) {
		this();
		while(i.hasNext()) {
			this.add(i.nextDouble());
		}
	}
	
	//public
	public static DoubleArrayList wrap(final double a[], final int length) {
		if (length > a.length) {
			throw new IllegalArgumentException( "The specified length (" + length + ") is greater than the array size (" + a.length + ")" );
		}
		final DoubleArrayList l = new DoubleArrayList(a, false);
		l.size = length;
		return l;
	}
	public static DoubleArrayList wrap(final double a[]) {
		return wrap(a, a.length);
	}
	
	public double[] elements() {
		return a;
	}
	
	public void ensureCapacity(final int capacity) {
		a = DoubleArrays.ensureCapacity(a, capacity, size);
		if (ASSERTS) {
			assert size <= a.length;
		}
	}
	
	public void add(final int index, final double k) {
		ensureIndex(index);
		grow(size + 1);
		if (index != size) {
			System.arraycopy(a, index, a, index + 1, size - index);
		}
		a[index] = k;
		size++;
		if (ASSERTS) {
			assert size <= a.length;
		}
	}
	public boolean add(final double k) {
		grow(size + 1);
		a[size++] = k;
		if (ASSERTS) {
			assert size <= a.length;
		}
		return true;
	}
	
	public double getDouble(final int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException( "Index (" + index + ") is greater than or equal to list size (" + size + ")" );
		}
		return a[index];
	}
	public int indexOf(final double k) {
		for (int i = 0; i < size; i++) {
			if (k == a[i]) {
				return i;
			}
		}
		return -1;
	}
	public int lastIndexOf(final double k) {
		for (int i = size; i-- != 0;) {
			if (k == a[i]) {
				return i;
			}
		}
		return -1;
	}
	
	public double removeDouble(final int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException( "Index (" + index + ") is greater than or equal to list size (" + size + ")" );
		}
		final double old = a[index];
		size--;
		if (index != size) {
			System.arraycopy(a, index + 1, a, index, size - index);
		}
		if (ASSERTS) {
			assert size <= a.length;
		}
		return old;
	}
	public boolean rem(final double k) {
		int index = indexOf(k);
		if (index == -1) {
			return false;
		}
		removeDouble(index);
		if (ASSERTS) {
			assert size <= a.length;
		}
		return true;
	}
	public double set(final int index, final double k) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
		}
		double old = a[index];
		a[index] = k;
		return old;
	}
	
	public void clear() {
		size = 0;
		if (ASSERTS) {
			assert size <= a.length;
		}
	}
	public int size() {
		return size;
	}
	public void size(final int size) {
		if (size > a.length) {
			ensureCapacity(size);
		}
		if (size > this.size) {
			DoubleArrays.fill(a, this.size, size, 0.0d);
		}
		this.size = size;
	}
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void trim() {
		trim(0);
	}
	public void trim(final int n) {
		if (n >= a.length || size == a.length) {
			return;
		}
		final double t[] = new double[Math.max(n, size)];
		System.arraycopy(a, 0, t, 0, size);
		a = t;
		if (ASSERTS) {
			assert size <= a.length;
		}
	}
	
	public void getElements(final int from, final double[] a, final int offset, final int length) {
		DoubleArrays.ensureOffsetLength(a, offset, length);
		System.arraycopy(this.a, from, a, offset, length);
	}
	public void removeElements(final int from, final int to) {
		Arrays.ensureFromTo(size, from, to);
		System.arraycopy(a, to, a, from, size - to);
		size -= (to - from);
	}
	public void addElements(final int index, final double a[], final int offset, final int length) {
		ensureIndex(index);
		DoubleArrays.ensureOffsetLength(a, offset, length);
		grow(size + length);
		System.arraycopy(this.a, index, this.a, index + length, size - index);
		System.arraycopy(a, offset, this.a, index, length);
		size += length;
	}
	
	public double[] toDoubleArray(double a[]) {
		if (a == null || a.length < size) {
			a = new double[size];
		}
		System.arraycopy(this.a, 0, a, 0, size);
		return a;
	}
	
	public boolean addAll(int index, final DoubleCollection c) {
		ensureIndex(index);
		int n = c.size();
		if (n == 0) {
			return false;
		}
		
		grow(size + n);
		if (index != size) {
			System.arraycopy(a, index, a, index + n, size - index);
		}
		final DoubleIterator i = c.iterator();
		size += n;
		while (n-- != 0) {
			a[index++] = i.nextDouble();
		}
		if (ASSERTS) {
			assert size <= a.length;
		}
		
		return true;
	}
	public boolean addAll(final int index, final DoubleList l) {
		ensureIndex(index);
		final int n = l.size();
		if (n == 0) {
			return false;
		}
		
		grow(size + n);
		if (index != size) {
			System.arraycopy(a, index, a, index + n, size - index);
		}
		l.getElements(0, a, index, n);
		size += n;
		if (ASSERTS) {
			assert size <= a.length;
		}
		
		return true;
	}
	
	public DoubleListIterator listIterator(final int index) {
		ensureIndex(index);
		return new AbstractDoubleListIterator() {
			int pos = index, last = -1;
			public boolean hasNext() {
				return pos < size;
			}
			public boolean hasPrevious() {
				return pos > 0;
			}
			public double nextDouble() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return a[last = pos++];
			}
			public double previousDouble() {
				if (!hasPrevious()) {
					throw new NoSuchElementException();
				}
				return a[last = --pos];
			}
			public int nextIndex() {
				return pos;
			}
			public int previousIndex() {
				return pos - 1;
			}
			public void add(double k) {
				if (last == -1) {
					throw new IllegalStateException();
				}
				DoubleArrayList.this.add(pos++, k);
				last = -1;
			}
			public void set( double k ) {
				if (last == -1) {
					throw new IllegalStateException();
				}
				DoubleArrayList.this.set(last, k);
			}
			public void remove() {
				if (last == -1) {
					throw new IllegalStateException();
				}
				DoubleArrayList.this.removeDouble(last);
				if (last < pos) {
					pos--;
				}
				last = -1;
			}
		};
	}
	public DoubleArrayList clone() {
		DoubleArrayList c = new DoubleArrayList(size);
		System.arraycopy(a, 0, c.a, 0, size);
		c.size = size;
		return c;
	}
	
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof DoubleArrayList)) {
			return false;
		}
		
		return equals((DoubleArrayList) o);
	}
	public boolean equals(final DoubleArrayList l) {
		if (l == this) {
			return true;
		}
		
		int s = size();
		if (s != l.size()) {
			return false;
		}
		final double[] a1 = a;
		final double[] a2 = l.a;
		
		while(s-- != 0 ) {
			if (a1[ s ] != a2[s]) {
				return false;
			}
		}
		
		return true;
	}
	public int compareTo(final DoubleArrayList l) {
		final int s1 = size();
		final int s2 = l.size();
		final double a1[] = a;
		final double a2[] = l.a;
		double e1;
		double e2;
		int r;
		int i;
		
		for (i = 0; i < s1 && i < s2; i++) {
			e1 = a1[ i ];
			e2 = a2[ i ];
			if ((r = (e1 < e2 ? -1 : (e1 == e2 ? 0 : 1))) != 0) {
				return r;
			}
		}
		
		return i < s2 ? -1 : (i < s1 ? 1 : 0);
	}
	
	//private
	private void grow(final int capacity) {
		a = DoubleArrays.grow(a, capacity, size);
		if (ASSERTS) {
			assert size <= a.length;
		}
	}
}
