package ninja.egg82.patterns;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import ninja.egg82.events.ExpireEventArgs;
import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Pair;
import ninja.egg82.patterns.tuples.Unit;
import ninja.egg82.utils.ReflectUtil;

public class ExpiringRegistry<K> implements IExpiringRegistry<K> {
	//vars
	private Class<K> keyClass = null;
	private K[] keyCache = null;
	private volatile boolean keysDirty = false;
	private ExpiringMap<K, Pair<Class<?>, Unit<Object>>> registry = null;
	private ConcurrentHashMap<Unit<Object>, K> reverseRegistry = new ConcurrentHashMap<Unit<Object>, K>();
	
	private final EventHandler<ExpireEventArgs<K>> expire = new EventHandler<ExpireEventArgs<K>>();
	
	//constructor
	public ExpiringRegistry(Class<K> keyClass, long defaultRegisterExpirationTimeMilliseconds) {
		this(keyClass, defaultRegisterExpirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	public ExpiringRegistry(K[] keyArray, long defaultRegisterExpirationTimeMilliseconds) {
		this(keyArray, defaultRegisterExpirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	@SuppressWarnings("unchecked")
	public ExpiringRegistry(Class<K> keyClass, long defaultRegisterExpirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = keyClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTimeMilliseconds, TimeUnit.MILLISECONDS)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener((k, v) -> onRegisterExpiration((K) k, (Pair<Class<?>, Unit<Object>>) v))
			.variableExpiration()
			.build();
	}
	@SuppressWarnings("unchecked")
	public ExpiringRegistry(K[] keyArray, long defaultRegisterExpirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = (Class<K>) keyArray.getClass().getComponentType();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTimeMilliseconds, TimeUnit.MILLISECONDS)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener((k, v) -> onRegisterExpiration((K) k, (Pair<Class<?>, Unit<Object>>) v))
			.variableExpiration()
			.build();
	}
	
	//public
	public final EventHandler<ExpireEventArgs<K>> onExpire() {
		return expire;
	}
	
	public final void setRegister(K key, Object data) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit));
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty = true;
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final void setRegister(K key, Object data, long expirationTimeMilliseconds) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit), expirationTimeMilliseconds, TimeUnit.MILLISECONDS);
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty = true;
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final void setRegister(K key, Object data, long expirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy policy) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		Unit<Object> unit = new Unit<Object>(data);
		registry.put(key, new Pair<Class<?>, Unit<Object>>((data != null) ? data.getClass() : null, unit), ExpirationPolicy.valueOf(policy.name()), expirationTimeMilliseconds, TimeUnit.MILLISECONDS);
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty = true;
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(unit, key);
	}
	public final void setRegisterExpiration(K key, long expirationTimeMilliseconds) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		registry.setExpiration(key, expirationTimeMilliseconds, TimeUnit.MILLISECONDS);
	}
	public final void setRegisterPolicy(K key, ninja.egg82.enums.ExpirationPolicy policy) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		registry.setExpirationPolicy(key, ExpirationPolicy.valueOf(policy.name()));
	}
	public final long getExpirationTime(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		try {
			return registry.getExpiration(key);
		} catch (Exception ex) {
			
		}
		
		return -1L;
	}
	public final long getTimeRemaining(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		try {
			return registry.getExpectedExpiration(key);
		} catch (Exception ex) {
			
		}
		
		return -1L;
	}
	public final void resetExpirationTime(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		registry.resetExpiration(key);
	}
	public final Object removeRegister(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty = true;
			return pair.getRight().getType();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final <T> T removeRegister(K key, Class<T> type) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Unit<Object>> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty = true;
			
			if (pair.getRight().getType() == null) {
				return null;
			}
			
			if (!ReflectUtil.doesExtend(type, pair.getLeft())) {
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
			throw new ArgumentNullException("key");
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
			throw new ArgumentNullException("key");
		}
		if (type == null) {
			throw new ArgumentNullException("type");
		}
		
		Pair<Class<?>, Unit<Object>> result = registry.get(key);
		
		if (result != null) {
			Object data = result.getRight().getType();
			if (data == null) {
				return null;
			}
			if (!ReflectUtil.doesExtend(type, result.getLeft())) {
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
			throw new ArgumentNullException("key");
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
		keysDirty = false;
	}
	
	@SuppressWarnings("unchecked")
	public final K[] getKeys() {
		if (keysDirty) {
			keyCache = registry.keySet().toArray((K[]) Array.newInstance(keyClass, 0));
			keysDirty = false;
		}
		return keyCache.clone();
	}
	
	//private
	private final void onRegisterExpiration(K key, Pair<Class<?>, Unit<Object>> value) {
		reverseRegistry.remove(value.getRight());
		keysDirty = true;
		expire.invoke(this, new ExpireEventArgs<K>(key, value.getLeft(), value.getRight().getType()));
	}
}
