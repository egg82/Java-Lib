package ninja.egg82.primitive.doubles;

import it.unimi.dsi.fastutil.Arrays;

public class DoubleArrays {
	//vars
	public final static double[] EMPTY_ARRAY = new double[0];
	
	//constructor
	private DoubleArrays() {
		
	}
	
	//public
	public static double[] ensureCapacity(final double[] array, final int length) {
		if (length > array.length) {
			final double t[] = new double[length];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static double[] ensureCapacity(final double[] array, final int length, final int preserve) {
		if (length > array.length) {
			final double t[] = new double[length];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static double[] grow(final double[] array, final int length) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final double t[] = new double[newLength];
			System.arraycopy(array, 0, t, 0, array.length);
			return t;
		}
		return array;
	}
	public static double[] grow(final double[] array, final int length, final int preserve) {
		if (length > array.length) {
			final int newLength = (int) Math.max(Math.min(2L * array.length, Arrays.MAX_ARRAY_SIZE), length);
			final double t[] = new double[newLength];
			System.arraycopy(array, 0, t, 0, preserve);
			return t;
		}
		return array;
	}
	
	public static double[] trim(final double[] array, final int length) {
		if (length >= array.length) {
			return array;
		}
		
		final double t[] = length == 0 ? EMPTY_ARRAY : new double[length];
		System.arraycopy(array, 0, t, 0, length);
		return t;
	}
	
	public static double[] setLength(final double[] array, final int length) {
		if (length == array.length) {
			return array;
		}
		if (length < array.length) {
			return trim(array, length);
		}
		return ensureCapacity(array, length);
	}
	
	public static double[] copy(final double[] array, final int offset, final int length) {
		ensureOffsetLength(array, offset, length);
		final double[] a = length == 0 ? EMPTY_ARRAY : new double[length];
		System.arraycopy(array, offset, a, 0, length);
		return a;
	}
	public static double[] copy(final double[] array) {
		return array.clone();
	}
	
	public static void fill(final double[] array, final double value) {
		int i = array.length;
		while (i-- != 0) {
			array[i] = value;
		}
	}
	public static void fill(final double[] array, final int from, int to, final double value) {
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
	
	public static boolean equals(final double[] a1, final double a2[]) {
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
	
	public static void ensureFromTo(final double[] a, final int from, final int to) {
		Arrays.ensureFromTo(a.length, from, to);
	}
	
	public static void ensureOffsetLength(final double[] a, final int offset, final int length) {
		Arrays.ensureOffsetLength(a.length, offset, length);
	}
	
	//private
	
}
