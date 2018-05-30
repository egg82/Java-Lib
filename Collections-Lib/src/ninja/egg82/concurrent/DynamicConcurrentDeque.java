package ninja.egg82.concurrent;

import java.util.concurrent.ConcurrentLinkedDeque;

public class DynamicConcurrentDeque<T> extends ConcurrentLinkedDeque<T> implements IConcurrentDeque<T> {
	//vars
	private static final long serialVersionUID = -8046163920520693918L;
	
	//constructor
	public DynamicConcurrentDeque() {
		
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
