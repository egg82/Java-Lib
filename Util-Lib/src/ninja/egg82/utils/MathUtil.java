package ninja.egg82.utils;

public final class MathUtil {
	//vars
	
	//constructor
	public MathUtil() {
		
	}
	
	//public
	public static double random(double min, double max) {
		return Math.random() * (max - min) + min;
	}
	public static int fairRoundedRandom(int min, int max) {
		int num;
		max++;
		
		do {
			num = (int) Math.floor(Math.random() * (max - min) + min);
		} while (num > max - 1);
		
		return num;
	}
	
	public static double clamp(double min, double max, double val) {
		return Math.min(max, Math.max(min, val));
	}
	public static float clamp(float min, float max, float val) {
		return Math.min(max, Math.max(min, val));
	}
	public static int clamp(int min, int max, int val) {
		return Math.min(max, Math.max(min, val));
	}
	public static short clamp(short min, short max, short val) {
		return (short) Math.min(max, Math.max(min, val));
	}
	public static long clamp(long min, long max, long val) {
		return Math.min(max, Math.max(min, val));
	}
	public static byte clamp(byte min, byte max, byte val) {
		return (byte) Math.min(max, Math.max(min, val));
	}
	
	public static int toXY(int width, int x, int y) {
		return y * width + x;
	}
	public static int toX(int width, int xy) {
		return xy % width;
	}
	public static int toY(int width, int xy) {
		return (int) Math.floor(xy / width);
	}
	
	public static int upperPowerOfTwo(int v) {
		if (v < 0) {
			v = 0;
		}
		
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		return v;
	}
	public static long upperPowerOfTwo(long v) {
		if (v < 0) {
			v = 0;
		}
		
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v |= v >> 32;
		v++;
		return v;
	}
	
	//private
	
}