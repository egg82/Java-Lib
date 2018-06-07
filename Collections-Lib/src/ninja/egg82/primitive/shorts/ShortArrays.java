package ninja.egg82.primitive.shorts;

import it.unimi.dsi.fastutil.Arrays;

public class ShortArrays {
	//vars
	public final static short[] EMPTY_ARRAY = new short[0];
	
	//constructor
	private ShortArrays() {
		
	}
	
	//public
	public static short[] ensureCapacity(final short[] array, final int length) {
		if (length > array.length) {
			final short t[] = new short[length];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static short[] ensureCapacity(final short[] array, final int length, final int preserve) {
		if (length > array.length) {
			final short t[] = new short[length];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static short[] grow(final short[] array, final int length) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final short t[] = new short[newLength];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static short[] grow(final short[] array, final int length, final int preserve) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final short t[] = new short[newLength];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static short[] trim(final short[] array, final int length) {
		if (length >= array.length) {
			return array;
		}
		
		final short t[] = length == 0 ? EMPTY_ARRAY : new short[length];
		System.arraycopy(array, 0, t, 0, length);
		return t;
	}
	
	public static short[] setLength(final short[] array, final int length) {
		if (length == array.length) {
			return array;
		}
		if (length < array.length) {
			return trim(array, length);
		}
		return ensureCapacity(array, length);
	}
	
	public static short[] copy(final short[] array, final int offset, final int length) {
		ensureOffsetLength(array, offset, length);
		final short[] a = length == 0 ? EMPTY_ARRAY : new short[length];
		System.arraycopy(array, offset, a, 0, length);
		return a;
	}
	public static short[] copy(final short[] array) {
		return array.clone();
	}
	
	public static void fill(final short[] array, final short value) {
		int i = array.length;
		while (i-- != 0) {
			array[i] = value;
		}
	}
	public static void fill(final short[] array, final int from, int to, final short value) {
		ensureFromTo(array, from, to);
		if (from == 0) {
			while (to-- != 0) {
				array[to] = value;
			}
		} else {
			for (int i = from; i < to; i++) {
				array[i] = value;
			}
		}
	}
	
	public static boolean equals(final short[] a1, final short a2[]) {
		int i = a1.length;
		if (i != a2.length) {
			return false;
		}
		while (i-- != 0) {
			if (!((a1[i]) == (a2[i]))) {
				return false;
			}
		}
		return true;
	}
	
	public static void ensureFromTo(final short[] a, final int from, final int to) {
		Arrays.ensureFromTo(a.length, from, to);
	}
	
	public static void ensureOffsetLength(final short[] a, final int offset, final int length) {
		Arrays.ensureOffsetLength(a.length, offset, length);
	}
	
	//private
	
}
