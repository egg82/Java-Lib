package ninja.egg82.patterns.prototypes;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public final class PrototypeFactory implements IPrototypeFactory {
	//vars
	private UnifiedMap<String, IPrototype> masterInstances = new UnifiedMap<String, IPrototype>();
	
	//constructor
	public PrototypeFactory() {
		
	}
	
	//public
	public synchronized void addPrototype(String name, IPrototype prototype) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		if (prototype == null) {
			throw new IllegalArgumentException("prototype cannot be null");
		}
		
		masterInstances.put(name, prototype);
	}
	public synchronized void removePrototype(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		masterInstances.remove(name);
	}
	public synchronized boolean hasPrototype(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		return masterInstances.containsKey(name);
	}
	public synchronized IPrototype createInstance(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		
		IPrototype masterInstance = masterInstances.get(name);
		
		if (masterInstance != null) {
			return masterInstance.clone();
		}
		return null;
	}
	public synchronized IPrototype[] createInstances(String name, int numInstances) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		
		IPrototype masterInstance = masterInstances.get(name);
		
		if (masterInstance != null) {
			IPrototype[] instances = new IPrototype[numInstances];
			for (int i = 0; i < numInstances; i++) {
				instances[i] = masterInstance.clone();
			}
			return instances;
		}
		return null;
	}
	
	//private

}
