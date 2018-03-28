package ninja.egg82.concurrent;

import ninja.egg82.core.BackingConcurrentLinkedHashSet;

public class DynamicConcurrentLinkedSet<T> extends BackingConcurrentLinkedHashSet<T> implements IConcurrentSet<T> {
	//vars
	private static final long serialVersionUID = 3839075247938018178L;
	
	//constructor
	public DynamicConcurrentLinkedSet() {
		
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
