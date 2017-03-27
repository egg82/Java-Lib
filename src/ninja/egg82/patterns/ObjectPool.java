package ninja.egg82.patterns;

import java.util.ArrayDeque;

import ninja.egg82.patterns.prototypes.IPrototype;

public final class ObjectPool<T extends IPrototype> {
	//vars
	private ArrayDeque<T> pool = new ArrayDeque<T>();
	private T masterInstance;
	private boolean isDynamic = true;
	
	//constructor
	public ObjectPool(T masterInstance, int numInstances) {
		this(masterInstance, numInstances, true);
	}
	public ObjectPool(T masterInstance, int numInstances, boolean isDynamic) {
		if (masterInstance == null) {
			throw new IllegalArgumentException("masterInstance cannot be null.");
		}
		if (numInstances < 0) {
			numInstances = 0;
		}
		
		this.masterInstance = masterInstance;
		this.isDynamic = isDynamic;
		addInstances(numInstances);
	}
	
	//public
	public synchronized int getNumFreeInstances() {
		return pool.size();
	}
	public boolean isDynamic() {
		return isDynamic;
	}
	
	@SuppressWarnings("unchecked")
	public T getObject() {
		return (pool.isEmpty()) ? ((isDynamic) ? (T) masterInstance.clone() : null) : pool.pop();
	}
	public void returnObject(T obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj cannot be null.");
		}
		pool.add(obj);
	}
	
	@SuppressWarnings("unchecked")
	public void addInstances(int numInstances) {
		if (numInstances <= 0) {
			return;
		}
		for (int i = 0; i < numInstances; i++) {
			pool.add((T) masterInstance.clone());
		}
	}
	
	public void clear() {
		if (pool.isEmpty()) {
			return;
		}
		
		pool.clear();
		System.gc();
	}
	
	//private

}
