package com.egg82.utils;

public class MathUtil {
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
	
	public static int toXY(int width, int x, int y) {
		return y * width + x;
	}
	public static int toX(int width, int xy) {
		return xy % width;
	}
	public static int toY(int width, int xy) {
		return (int) Math.floor(xy / width);
	}
	
	//private
	
}