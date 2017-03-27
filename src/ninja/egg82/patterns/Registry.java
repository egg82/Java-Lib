package ninja.egg82.patterns;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

public class Registry implements IRegistry {
	//vars
	private String[] keyCache = new String[0];
	private UnifiedMap<String, Pair<Class<?>, Object>> registry = new UnifiedMap<String, Pair<Class<?>, Object>>();
	
	//constructor
	public Registry() {
		
	}
	
	//public
	public final synchronized void setRegister(String name, Class<?> type, Object data) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null.");
		}
		
		if (data == null) {
			registry.remove(name);
			keyCache = registry.keySet().toArray(new String[0]);
		} else {
			if (data.getClass() != type) {
				try {
					data = type.cast(data);
				} catch (Exception ex) {
					throw new RuntimeException("data type cannot be converted to the type specified.", ex);
				}
			}
			
			if (registry.containsKey(name)) {
				registry.put(name, Pair.of(type, data));
			} else {
				registry.put(name, Pair.of(type, data));
				keyCache = registry.keySet().toArray(new String[0]);
			}
		}
	}
	public final synchronized Object getRegister(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null.");
		}
		
		Pair<Class<?>, Object> result = registry.get(name);
		
		if (result != null) {
			return result.getRight();
		}
		return null;
	}
	public final synchronized Class<?> getRegisterClass(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null.");
		}
		
		Pair<Class<?>, Object> result = registry.get(name);
		
		if (result != null) {
			return result.getLeft();
		}
		return null;
	}
	public final synchronized boolean hasRegister(String name) {
		if (name == null) {
			return false;
		}
		return registry.containsKey(name);
	}
	
	public final synchronized void clear() {
		registry.clear();
		keyCache = new String[0];
	}
	
	public final String[] getRegistryNames() {
		return keyCache.clone();
	}
	
	//private
	
}
