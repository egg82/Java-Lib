package ninja.egg82.primitive.chars;

import it.unimi.dsi.fastutil.Arrays;

public class CharArrays {
	//vars
	public final static char[] EMPTY_ARRAY = new char[0];
	
	//constructor
	private CharArrays() {
		
	}
	
	//public
	public static char[] ensureCapacity(final char[] array, final int length) {
		if (length > array.length) {
			final char t[] = new char[length];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static char[] ensureCapacity(final char[] array, final int length, final int preserve) {
		if (length > array.length) {
			final char t[] = new char[length];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static char[] grow(final char[] array, final int length) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final char t[] = new char[newLength];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static char[] grow(final char[] array, final int length, final int preserve) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final char t[] = new char[newLength];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static char[] trim(final char[] array, final int length) {
		if (length >= array.length) {
			return array;
		}
		
		final char t[] = length == 0 ? EMPTY_ARRAY : new char[length];
		System.arraycopy(array, 0, t, 0, length);
		return t;
	}
	
	public static char[] setLength(final char[] array, final int length) {
		if (length == array.length) {
			return array;
		}
		if (length < array.length) {
			return trim(array, length);
		}
		return ensureCapacity(array, length);
	}
	
	public static char[] copy(final char[] array, final int offset, final int length) {
		ensureOffsetLength(array, offset, length);
		final char[] a = length == 0 ? EMPTY_ARRAY : new char[length];
		System.arraycopy(array, offset, a, 0, length);
		return a;
	}
	public static char[] copy(final char[] array) {
		return array.clone();
	}
	
	public static void fill(final char[] array, final char value) {
		int i = array.length;
		while (i-- != 0) {
			array[i] = value;
		}
	}
	public static void fill(final char[] array, final int from, int to, final char value) {
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
	
	public static boolean equals(final char[] a1, final char a2[]) {
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
	
	public static void ensureFromTo(final char[] a, final int from, final int to) {
		Arrays.ensureFromTo(a.length, from, to);
	}
	
	public static void ensureOffsetLength(final char[] a, final int offset, final int length) {
		Arrays.ensureOffsetLength(a.length, offset, length);
	}
	
	//private
	
}
