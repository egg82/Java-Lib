package ninja.egg82.concurrent;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FixedConcurrentDeque<T> extends ConcurrentLinkedDeque<T> implements IConcurrentDeque<T> {
	//vars
	private static final long serialVersionUID = 5943575711394590373L;
	private int capacity = 0;
	
	//constructor
	public FixedConcurrentDeque(int capacity) {
		this.capacity = capacity;
	}
	
	//public
	public final int getRemainingCapacity() {
		return Math.max(capacity - size(), 0);
	}
	public final int getCapacity() {
		return capacity;
	}
	
	public final void addFirst(T e) {
		if (size() >= capacity) {
			throw new IllegalStateException("Deque full");
		}
		super.addFirst(e);
	}
	public final void addLast(T e) {
		if (size() >= capacity) {
			throw new IllegalStateException("Deque full");
		}
		super.addLast(e);
	}
	
	public final boolean offerFirst(T e) {
		if (size() >= capacity) {
			return false;
		}
		return super.offerFirst(e);
	}
	public final boolean offerLast(T e) {
		if (size() >= capacity) {
			return false;
		}
		return super.offerLast(e);
	}
	
	public final boolean add(T e) {
		if (size() >= capacity) {
			throw new IllegalStateException("Deque full");
		}
		return super.add(e);
	}
	public final boolean offer(T e) {
		if (size() >= capacity) {
			return false;
		}
		return super.offer(e);
	}
	
	public final void push(T e) {
		if (size() >= capacity) {
			throw new IllegalStateException("Deque full");
		}
		super.push(e);
	}
	
	public final boolean addAll(Collection<? extends T> c) {
		if (size() > capacity - c.size()) {
			throw new IllegalStateException("Deque full");
		}
		return super.addAll(c);
	}
	
	//private

}
