package ninja.egg82.patterns;

import java.util.HashMap;

import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.tuples.Pair;
import ninja.egg82.utils.ReflectUtil;

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
			throw new ArgumentNullException("name");
		}
		if (type == null) {
			throw new ArgumentNullException("type");
		}
		
		if (!ReflectUtil.doesExtend(type, data.getClass())) {
			try {
				data = type.cast(data);
			} catch (Exception ex) {
				throw new RuntimeException("data type cannot be converted to the type specified.", ex);
			}
		}
		
		Pair<Class<?>, Object> pair = registry.get(name);
		registry.put(name, new Pair<Class<?>, Object>(type, data));
		reverseRegistry.put(data, name);
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty = true;
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
	}
	public final synchronized void removeRegister(String name) {
		if (name == null) {
			throw new ArgumentNullException("name");
		}
		
		Pair<Class<?>, Object> pair = registry.get(name);
		if (pair != null) {
			registry.remove(name);
			reverseRegistry.remove(pair.getRight());
			keysDirty = true;
		}
	}
	
	public final synchronized Object getRegister(String name) {
		if (name == null) {
			throw new ArgumentNullException("name");
		}
		
		Pair<Class<?>, Object> result = registry.get(name);
		
		if (result != null) {
			return result.getRight();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final synchronized <T> T getRegister(String name, Class<T> type) {
		if (name == null) {
			throw new ArgumentNullException("name");
		}
		if (type == null) {
			throw new ArgumentNullException("type");
		}
		
		Pair<Class<?>, Object> result = registry.get(name);
		
		if (result != null) {
			Object data = result.getRight();
			if (data == null) {
				return null;
			}
			if (!ReflectUtil.doesExtend(type, data.getClass())) {
				try {
					return type.cast(data);
				} catch (Exception ex) {
					throw new RuntimeException("data type cannot be converted to the type specified.", ex);
				}
			} else {
				return (T) data;
			}
		}
		return null;
	}
	public final synchronized String getName(Object data) {
		return reverseRegistry.get(data);
	}
	public final synchronized Class<?> getRegisterClass(String name) {
		if (name == null) {
			throw new ArgumentNullException("name");
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
