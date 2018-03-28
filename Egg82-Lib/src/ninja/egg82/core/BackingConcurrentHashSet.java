package ninja.egg82.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BackingConcurrentHashSet<E> implements Set<E>, Cloneable, Serializable {
	//vars
	private static final long serialVersionUID = 6476080469111198453L;
	
	private Set<E> backingSet = ConcurrentHashMap.newKeySet();
	
	//constructor
	public BackingConcurrentHashSet() {
		
	}
	
	public int size() {
		return backingSet.size();
	}
	public boolean isEmpty() {
		return backingSet.isEmpty();
	}
	
	public boolean contains(Object o) {
		return backingSet.contains(o);
	}
	public boolean containsAll(Collection<?> c) {
		return backingSet.containsAll(c);
	}
	
	public Object[] toArray() {
		return backingSet.toArray();
	}
	public <T> T[] toArray(T[] a) {
		return backingSet.toArray(a);
	}
	
	public boolean add(E e) {
		return backingSet.add(e);
	}
	public boolean addAll(Collection<? extends E> c) {
		return backingSet.addAll(c);
	}
	public boolean remove(Object o) {
		return backingSet.remove(o);
	}
	public boolean removeAll(Collection<?> c) {
		return backingSet.removeAll(c);
	}
	public boolean retainAll(Collection<?> c) {
		return backingSet.retainAll(c);
	}
	
	public Iterator<E> iterator() {
		return backingSet.iterator();
	}
	public void clear() {
		backingSet.clear();
	}
	
	//public
	
	//private
	
}
