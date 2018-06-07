package ninja.egg82.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

public class BackingConcurrentLinkedHashSet<E> implements Set<E>, Cloneable, Serializable {
	//vars
	private static final long serialVersionUID = -3240657871339299109L;
	private ConcurrentLinkedHashMap<E, Boolean> backingMap = new ConcurrentLinkedHashMap.Builder<E, Boolean>().maximumWeightedCapacity(Integer.MAX_VALUE).build();
	
	//constructor
	public BackingConcurrentLinkedHashSet() {
		
	}
	
	public int size() {
		return backingMap.size();
	}
	public boolean isEmpty() {
		return backingMap.isEmpty();
	}
	
	public boolean contains(Object o) {
		return backingMap.containsKey(o);
	}
	public boolean containsAll(Collection<?> c) {
		return backingMap.keySet().containsAll(c);
	}
	
	public Object[] toArray() {
		return backingMap.ascendingKeySet().toArray();
	}
	public <T> T[] toArray(T[] a) {
		return backingMap.ascendingKeySet().toArray(a);
	}
	
	public boolean add(E e) {
		return (backingMap.putIfAbsent(e, Boolean.TRUE) == null) ? true : false;
	}
	public boolean addAll(Collection<? extends E> c) {
		return backingMap.keySet().addAll(c);
	}
	public boolean remove(Object o) {
		return (backingMap.remove(o) != null) ? true : false;
	}
	public boolean removeAll(Collection<?> c) {
		return backingMap.keySet().removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return backingMap.keySet().retainAll(c);
	}
	
	public Iterator<E> iterator() {
		return new WrappingIterator<E>(backingMap.keySet(), backingMap.ascendingKeySet().iterator());
	}
	public void clear() {
		backingMap.clear();
	}
	
	//public
	
	//private
	
}

final class WrappingIterator<E> implements Iterator<E> {
	//vars
	private Set<E> keySet = null;
	private Iterator<E> unmodifiableIterator = null;
	private E current = null;
	
	//constructor
	public WrappingIterator(Set<E> keySet, Iterator<E> unmodifiableIterator) {
		this.keySet = keySet;
		this.unmodifiableIterator = unmodifiableIterator;
	}
	
	public boolean hasNext() {
		return unmodifiableIterator.hasNext();
	}
	public E next() {
		current = unmodifiableIterator.next();
		return current;
	}
	public void remove() {
		if (current == null) {
			throw new IllegalStateException();
		}
		keySet.remove(current);
	}
	
	//public
	
	//private
	
}
