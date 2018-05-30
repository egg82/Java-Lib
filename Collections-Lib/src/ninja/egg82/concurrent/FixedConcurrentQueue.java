package ninja.egg82.concurrent;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FixedConcurrentQueue<T> extends ConcurrentLinkedQueue<T> implements IConcurrentQueue<T> {
	//vars
	private static final long serialVersionUID = -4677012945563206784L;
	private int capacity = 0;
	
	//constructor
	public FixedConcurrentQueue(int capacity) {
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
			throw new IllegalStateException("Queue full");
		}
		return super.add(e);
	}
	public final boolean offer(T e) {
		if (size() >= capacity) {
			return false;
		}
		return super.offer(e);
	}
	
	public final boolean addAll(Collection<? extends T> c) {
		if (size() > capacity - c.size()) {
			throw new IllegalStateException("Queue full");
		}
		return super.addAll(c);
	}
	
	//private

}
