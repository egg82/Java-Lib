package ninja.egg82.patterns.registries;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import ninja.egg82.core.CollectionsReflectUtil;
import ninja.egg82.events.VariableRegisterExpireEventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Unit;
import ninja.egg82.patterns.tuples.pair.Pair;

public class VariableExpiringRegistry<K> implements IVariableExpiringRegistry<K> {
	//vars
	private Class<K> keyClass = null;
	private K[] keyCache = null;
	private AtomicBoolean keysDirty = new AtomicBoolean(false);
	private ExpiringMap<K, Pair<Class<?>, Unit<Object>>> registry = null;
	private ConcurrentMap<Unit<Object>, K> reverseRegistry = new ConcurrentHashMap<Unit<Object>, K>();
	
	private final EventHandler<VariableRegisterExpireEventArgs<K>> expire = new EventHandler<VariableRegisterExpireEventArgs<K>>();
	
	//constructor
	public VariableExpiringRegistry(Class<K> keyClass, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit) {
		this(keyClass, defaultRegisterExpirationTime, defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	public VariableExpiringRegistry(K[] keyArray, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit) {
		this(keyArray, defaultRegisterExpirationTime, defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	@SuppressWarnings("unchecked")
	public VariableExpiringRegistry(Class<K> keyClass, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = keyClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTime, defaultExpirationUnit)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener(onExpiration)
			.variableExpiration()
			.build();
	}
	@SuppressWarnings("unchecked")
	public VariableExpiringRegistry(K[] keyArray, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = (Class<K>) keyArray.getClass().getComponentType();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTime, defaultExpirationUnit)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener(onExpiration)
			.variableExpiration()
			.build();
	}
	
	//public
	public final EventHandler<VariableRegisterExpireEventArgs<K>> onExpire() {
		return expire;
	}
	
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
	public final void setRegister(K key, Object data, long expirationTime, TimeUnit expirationUnit) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit), expirationTime, expirationUnit);
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final void setRegister(K key, Object data, long expirationTime, TimeUnit expirationUnit, ninja.egg82.enums.ExpirationPolicy policy) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit), ExpirationPolicy.valueOf(policy.name()), expirationTime, expirationUnit);
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final void setRegisterExpiration(K key, long expirationTime, TimeUnit expirationUnit) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		registry.setExpiration(key, expirationTime, expirationUnit);
	}
	public final void setRegisterPolicy(K key, ninja.egg82.enums.ExpirationPolicy policy) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		registry.setExpirationPolicy(key, ExpirationPolicy.valueOf(policy.name()));
	}
	public final long getExpirationTime(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		try {
			return registry.getExpiration(key);
		} catch (Exception ex) {
			
		}
		
		return -1L;
	}
	public final long getTimeRemaining(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		try {
			return registry.getExpectedExpiration(key);
		} catch (Exception ex) {
			
		}
		
		return -1L;
	}
	public final void resetExpirationTime(K key) {
		if (key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		registry.resetExpiration(key);
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
			}
			return (T) pair.getRight().getType();
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
			}
			return (T) data;
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
			keyCache = registry.keySet().toArray((K[]) Array.newInstance(keyClass, 0));
		}
		return keyCache.clone();
	}
	
	//private
	private final ExpirationListener<K, Pair<Class<?>, Unit<Object>>> onExpiration = new ExpirationListener<K, Pair<Class<?>, Unit<Object>>>() {
		public void expired(K key, Pair<Class<?>, Unit<Object>> value) {
			reverseRegistry.remove(value.getRight());
			keysDirty.set(true);
			expire.invoke(this, new VariableRegisterExpireEventArgs<K>(key, value.getLeft(), value.getRight().getType()));
		}
	};
}
