package ninja.egg82.patterns;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import ninja.egg82.exceptions.ArgumentNullException;

public final class FixedObjectPool<T> implements IObjectPool<T> {
	//vars
	private ConcurrentLinkedDeque<T> pool = null;
	private int capacity = 0;
	
	//constructor
	public FixedObjectPool(int capacity) {
		this.capacity = capacity;
		pool = new ConcurrentLinkedDeque<T>();
	}
	
	//public
	public int size() {
		return pool.size();
	}
	public int remainingCapacity() {
		return capacity - pool.size();
	}
	public int capacity() {
		return capacity;
	}
	
	public T popFirst() {
		return pool.pollFirst();
	}
	public T peekFirst() {
		return pool.peekFirst();
	}
	public T popLast() {
		return pool.pollLast();
	}
	public T peekLast() {
		return pool.peekLast();
	}
	public boolean add(T e) {
		if (e == null) {
			throw new ArgumentNullException("e");
		}
		if (remainingCapacity() == 0) {
			throw new IllegalStateException("Attempted to add element to full FixedObjectPool.");
		}
		
		return pool.add(e);
	}
	public boolean addFirst(T e) {
		if (e == null) {
			throw new ArgumentNullException("e");
		}
		if (remainingCapacity() == 0) {
			throw new IllegalStateException("Attempted to add element to full FixedObjectPool.");
		}
		
		pool.addFirst(e);
		return true;
	}
	public boolean addAll(Collection<? extends T> c) {
		if (c == null) {
			throw new ArgumentNullException("c");
		}
		
		c.removeIf((v) -> { return (v == null) ? true : false; });
		
		if (remainingCapacity() < c.size()) {
			throw new IllegalStateException("Attempted to add elements that would overfill FixedObjectPool.");
		}
		
		return pool.addAll(c);
	}
	
	public void clear() {
		if (pool.isEmpty()) {
			return;
		}
		
		pool.clear();
		System.gc();
		System.runFinalization();
	}
	
	public boolean isEmpty() {
		return pool.isEmpty();
	}
	public boolean contains(Object o) {
		return pool.contains(o);
	}
	public Iterator<T> iterator() {
		return pool.iterator();
	}
	public Object[] toArray() {
		return pool.toArray();
	}
	public <E> E[] toArray(E[] a) {
		return pool.toArray(a);
	}
	public boolean remove(Object o) {
		return pool.remove(o);
	}
	public boolean containsAll(Collection<?> c) {
		return pool.containsAll(c);
	}
	public boolean removeAll(Collection<?> c) {
		return pool.removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return pool.retainAll(c);
	}
	
	//private

}
