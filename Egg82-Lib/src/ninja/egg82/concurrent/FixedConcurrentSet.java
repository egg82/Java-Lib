package ninja.egg82.concurrent;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class FixedConcurrentSet<T> extends ConcurrentSkipListSet<T> implements IConcurrentSet<T> {
	//vars
	private static final long serialVersionUID = 2756891575591440938L;
	private int capacity = 0;
	
	//constructor
	public FixedConcurrentSet(int capacity) {
		this.capacity = capacity;
	}
	
	//public
	public final int getRemainingCapacity() {
		return Math.max(capacity - size(), 0);
	}
	public final int getCapacity() {
		return capacity;
	}
	
	public final boolean add(T e) {
		if (size() >= capacity) {
			throw new IllegalStateException("Set full");
		}
		return super.add(e);
	}
	public final boolean addAll(Collection<? extends T> c) {
		if (size() > capacity - c.size()) {
			throw new IllegalStateException("Set full");
		}
		return super.addAll(c);
	}
	
	//private

}
