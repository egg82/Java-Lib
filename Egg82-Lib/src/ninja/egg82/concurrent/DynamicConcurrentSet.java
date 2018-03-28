package ninja.egg82.concurrent;

import java.util.concurrent.ConcurrentSkipListSet;

public class DynamicConcurrentSet<T> extends ConcurrentSkipListSet<T> implements IConcurrentSet<T> {
	//vars
	private static final long serialVersionUID = 4002682950853337773L;
	
	//constructor
	public DynamicConcurrentSet() {
		
	}
	
	//public
	public final int getRemainingCapacity() {
		return Integer.MAX_VALUE - size();
	}
	public final int getCapacity() {
		return Integer.MAX_VALUE;
	}
	
	//private
	
}
