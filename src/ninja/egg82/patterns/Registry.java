package ninja.egg82.patterns;

import java.util.HashMap;

public class Registry implements IRegistry {
	//vars
	private String[] keyCache = new String[0];
	private boolean keysDirty = false;
	private HashMap<String, Pair<Class<?>, Object>> registry = new HashMap<String, Pair<Class<?>, Object>>();
	private HashMap<Object, String> reverseRegistry = new HashMap<Object, String>();
	
	//constructor
	public Registry() {
		
	}
	
	//public
	public final synchronized void setRegister(String name, Class<?> type, Object data) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null.");
		}
		
		if (data == null) {
			Pair<Class<?>, Object> pair = registry.get(name);
			if (pair != null) {
				registry.remove(name);
				reverseRegistry.remove(pair.getRight());
				keysDirty = true;
			}
		} else {
			if (data.getClass() != type) {
				try {
					data = type.cast(data);
				} catch (Exception ex) {
					throw new RuntimeException("data type cannot be converted to the type specified.", ex);
				}
			}
			
			Pair<Class<?>, Object> pair = registry.get(name);
			registry.put(name, new Pair<Class<?>, Object>(type, data));
			reverseRegistry.put(data, name);
			
			// Key didn't exist before. Added.
			if (pair == null) {
				keysDirty = true;
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
	public final synchronized String getName(Object data) {
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null.");
		}
		return reverseRegistry.get(data);
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
	public final synchronized boolean hasValue(Object data) {
		if (data == null) {
			return false;
		}
		return reverseRegistry.containsKey(data);
	}
	
	public final synchronized void clear() {
		registry.clear();
		reverseRegistry.clear();
		keyCache = new String[0];
		keysDirty = false;
	}
	
	public final String[] getRegistryNames() {
		if (keysDirty) {
			keyCache = registry.keySet().toArray(new String[0]);
			keysDirty = false;
		}
		return keyCache.clone();
	}
	
	//private
	
}
