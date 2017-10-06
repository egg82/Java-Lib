package ninja.egg82.patterns;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import ninja.egg82.events.ExpireEventArgs;
import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Pair;
import ninja.egg82.utils.ReflectUtil;

public class ExpiringRegistry<K> implements IExpiringRegistry<K> {
	//vars
	private Class<K> keyClass = null;
	private K[] keyCache = null;
	private boolean keysDirty = false;
	private Map<K, Pair<Class<?>, Object>> registry = null;
	private HashMap<Object, K> reverseRegistry = new HashMap<Object, K>();
	
	private final EventHandler<ExpireEventArgs<K>> expire = new EventHandler<ExpireEventArgs<K>>();
	
	//constructor
	public ExpiringRegistry(Class<K> keyClass, long registerExpirationTimeMilliseconds) {
		this(keyClass, registerExpirationTimeMilliseconds, ExpirationPolicy.CREATED);
	}
	@SuppressWarnings("unchecked")
	public ExpiringRegistry(Class<K> keyClass, long registerExpirationTimeMilliseconds, ExpirationPolicy expirationPolicy) {
		this.keyClass = keyClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(registerExpirationTimeMilliseconds, TimeUnit.MILLISECONDS)
			.expirationPolicy(expirationPolicy)
			.expirationListener((k, v) -> onRegisterExpiration((K) k, (Pair<Class<?>, Object>) v))
			.build();
	}
	
	//public
	public final EventHandler<ExpireEventArgs<K>> onExpire() {
		return expire;
	}
	
	public final synchronized void setRegister(K key, Object data) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Object> pair = registry.get(key);
		registry.put(key, new Pair<Class<?>, Object>((data != null) ? data.getClass() : null, data));
		
		if (pair == null) {
			// Key didn't exist before. Added.
			keysDirty = true;
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(pair.getRight());
		}
		
		reverseRegistry.put(data, key);
	}
	public final synchronized Object removeRegister(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Object> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty = true;
			return pair.getRight();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final synchronized <T> T removeRegister(K key, Class<T> type) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Object> pair = registry.get(key);
		if (pair != null) {
			registry.remove(key);
			reverseRegistry.remove(pair.getRight());
			keysDirty = true;
			
			if (pair.getRight() == null) {
				return null;
			}
			
			if (!ReflectUtil.doesExtend(type, pair.getLeft())) {
				try {
					return type.cast(pair.getRight());
				} catch (Exception ex) {
					throw new RuntimeException("data type cannot be converted to the type specified.", ex);
				}
			} else {
				return (T) pair.getRight();
			}
		}
		return null;
	}
	
	public final synchronized Object getRegister(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Object> result = registry.get(key);
		
		if (result != null) {
			return result.getRight();
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public final synchronized <T> T getRegister(K key, Class<T> type) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		if (type == null) {
			throw new ArgumentNullException("type");
		}
		
		Pair<Class<?>, Object> result = registry.get(key);
		
		if (result != null) {
			Object data = result.getRight();
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
	public final synchronized K getKey(Object data) {
		return reverseRegistry.get(data);
	}
	public final synchronized Class<?> getRegisterClass(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Pair<Class<?>, Object> result = registry.get(key);
		
		if (result != null) {
			return result.getLeft();
		}
		return null;
	}
	
	public final synchronized Class<K> getKeyClass() {
		return keyClass;
	}
	
	public final synchronized boolean hasRegister(K key) {
		if (key == null) {
			return false;
		}
		return registry.containsKey(key);
	}
	public final synchronized boolean hasValue(Object data) {
		return reverseRegistry.containsKey(data);
	}
	
	@SuppressWarnings("unchecked")
	public final synchronized void clear() {
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
	private final synchronized void onRegisterExpiration(K key, Pair<Class<?>, Object> value) {
		reverseRegistry.remove(value.getRight());
		keysDirty = true;
		expire.invoke(this, new ExpireEventArgs<K>(key, value.getLeft(), value.getRight()));
	}
}
