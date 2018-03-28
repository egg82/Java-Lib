package ninja.egg82.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DynamicConcurrentQueue<T> extends ConcurrentLinkedQueue<T> implements IConcurrentQueue<T> {
	//vars
	private static final long serialVersionUID = -5844512838100731365L;
	
	//constructor
	public DynamicConcurrentQueue() {
		
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
