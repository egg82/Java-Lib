package ninja.egg82.patterns.registries;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import ninja.egg82.core.CollectionsReflectUtil;
import ninja.egg82.patterns.tuples.Unit;
import ninja.egg82.patterns.tuples.pair.Pair;

public class VariableLinkedRegistry<K> implements IVariableRegistry<K> {
	//vars
	private Class<K> keyClass = null;
	private K[] keyCache = null;
	private AtomicBoolean keysDirty = new AtomicBoolean(false);
	private ConcurrentLinkedHashMap<K, Pair<Class<?>, Unit<Object>>> registry = new ConcurrentLinkedHashMap.Builder<K, Pair<Class<?>, Unit<Object>>>().maximumWeightedCapacity(Integer.MAX_VALUE).build();
	private ConcurrentMap<Unit<Object>, K> reverseRegistry = new ConcurrentLinkedHashMap.Builder<Unit<Object>, K>().maximumWeightedCapacity(Integer.MAX_VALUE).build();
	
	//constructor
	@SuppressWarnings("unchecked")
	public VariableLinkedRegistry(Class<K> keyClass) {
		this.keyClass = keyClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
	}
	@SuppressWarnings("unchecked")
	public VariableLinkedRegistry(K[] keyArray) {
		this.keyClass = (Class<K>) keyArray.getClass().getComponentType();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
	}
	
	//public
	public final void setRegister(K key, Object data) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit));
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final Object removeRegister(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty.set(true);
			return pair.getRight().getType();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final <T> T removeRegister(K key, Class<T> type) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty.set(true);
			
			if (pair.getRight().getType() == null) {
				return null;
			}
			
			if (!CollectionsReflectUtil.doesExtend(type, pair.getLeft())) {
				try {
					return type.cast(pair.getRight().getType());
				} catch (Exception ex) {
					throw new RuntimeException("data type cannot be converted to the type specified.", ex);
				}
			} else {
				return (T) pair.getRight().getType();
			}
		}
		return null;
	}
	
	public final Object getRegister(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> result = registry.get(key);
		
		if (result != null) {
			return result.getRight().getType();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final <T> T getRegister(K key, Class<T> type) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		if (type == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> result = registry.get(key);
		
		if (result != null) {
			Object data = result.getRight().getType();
			if (data == null) {
				return null;
			}
			if (!CollectionsReflectUtil.doesExtend(type, result.getLeft())) {
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
	public final K getKey(Object data) {
		return reverseRegistry.get(new Unit<Object>(data));
	}
	public final Class<?> getRegisterClass(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> result = registry.get(key);
		
		if (result != null) {
			return result.getLeft();
		}
		return null;
	}
	
	public final Class<K> getKeyClass() {
		return keyClass;
	}
	
	public final boolean hasRegister(K key) {
		if (key == null) {
			return false;
		}
		return registry.containsKey(key);
	}
	public final boolean hasValue(Object data) {
		return reverseRegistry.containsKey(new Unit<Object>(data));
	}
	
	@SuppressWarnings("unchecked")
	public final void clear() {
		registry.clear();
		reverseRegistry.clear();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		keysDirty.set(false);
	}
	
	@SuppressWarnings("unchecked")
	public final K[] getKeys() {
		if (keysDirty.getAndSet(false)) {
			keyCache = registry.ascendingKeySet().toArray((K[]) Array.newInstance(keyClass, 0));
		}
		return keyCache.clone();
	}
	
	//private
	
}
