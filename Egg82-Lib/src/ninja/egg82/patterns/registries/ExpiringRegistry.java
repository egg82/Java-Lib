package ninja.egg82.patterns.registries;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import ninja.egg82.events.ExpireEventArgs;
import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Unit;

public class ExpiringRegistry<K, V> implements IExpiringRegistry<K, V> {
	//vars
	private Class<K> keyClass = null;
	private Class<V> valueClass = null;
	private K[] keyCache = null;
	private AtomicBoolean keysDirty = new AtomicBoolean(false);
	private ExpiringMap<K, Unit<V>> registry = null;
	private ConcurrentMap<Unit<V>, K> reverseRegistry = new ConcurrentHashMap<Unit<V>, K>();
	
	private final EventHandler<ExpireEventArgs<K, V>> expire = new EventHandler<ExpireEventArgs<K, V>>();
	
	//constructor
	public ExpiringRegistry(Class<K> keyClass, Class<V> valueClass, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit) {
		this(keyClass, valueClass, defaultRegisterExpirationTime, defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	public ExpiringRegistry(K[] keyArray, V[] valueArray, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit) {
		this(keyArray, valueArray, defaultRegisterExpirationTime, defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy.CREATED);
	}
	@SuppressWarnings("unchecked")
	public ExpiringRegistry(Class<K> keyClass, Class<V> valueClass, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTime, defaultExpirationUnit)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener(onExpiration)
			.variableExpiration()
			.build();
	}
	@SuppressWarnings("unchecked")
	public ExpiringRegistry(K[] keyArray, V[] valueArray, long defaultRegisterExpirationTime, TimeUnit defaultExpirationUnit, ninja.egg82.enums.ExpirationPolicy defaultExpirationPolicy) {
		this.keyClass = (Class<K>) keyArray.getClass().getComponentType();
		this.valueClass = (Class<V>) valueArray.getClass().getComponentType();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
		
		registry = ExpiringMap.builder()
			.expiration(defaultRegisterExpirationTime, defaultExpirationUnit)
			.expirationPolicy(ExpirationPolicy.valueOf(defaultExpirationPolicy.name()))
			.expirationListener(onExpiration)
			.variableExpiration()
			.build();
	}
	
	//public
	public final EventHandler<ExpireEventArgs<K, V>> onExpire() {
		return expire;
	}
	
	public final void setRegister(K key, V data) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Unit<V> newValue = new Unit<V>(data);
		Unit<V> oldValue = registry.put(key, newValue);
		
		if (oldValue == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(oldValue);
		}
		
		reverseRegistry.put(newValue, key);
	}
	public final void setRegister(K key, V data, long expirationTime, TimeUnit expirationUnit) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Unit<V> newValue = new Unit<V>(data);
		Unit<V> oldValue = registry.put(key, newValue, expirationTime, expirationUnit);
		
		if (oldValue == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(oldValue);
		}
		
		reverseRegistry.put(newValue, key);
	}
	public final void setRegister(K key, V data, long expirationTime, TimeUnit expirationUnit, ninja.egg82.enums.ExpirationPolicy policy) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Unit<V> newValue = new Unit<V>(data);
		Unit<V> oldValue = registry.put(key, newValue, ExpirationPolicy.valueOf(policy.name()), expirationTime, expirationUnit);
		
		if (oldValue == null) {
			// Key didn't exist before. Added.
			keysDirty.set(true);
		} else {
			// Key existed before. Need to remove old value->key from reverse registry.
			reverseRegistry.remove(oldValue);
		}
		
		reverseRegistry.put(newValue, key);
	}
	public final void setRegisterExpiration(K key, long expirationTime, TimeUnit expirationUnit) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		registry.setExpiration(key, expirationTime, expirationUnit);
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
	public final V removeRegister(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Unit<V> oldValue = registry.remove(key);
		if (oldValue != null) {
			reverseRegistry.remove(oldValue);
			keysDirty.set(true);
			return oldValue.getType();
		}
		return null;
	}
	
	public final V getRegister(K key) {
		if (key == null) {
			throw new ArgumentNullException("key");
		}
		
		Unit<V> result = registry.get(key);
		
		if (result != null) {
			return result.getType();
		}
		return null;
	}
	public final K getKey(V data) {
		return reverseRegistry.get(new Unit<V>(data));
	}
	
	public final Class<K> getKeyClass() {
		return keyClass;
	}
	public final Class<V> getValueClass() {
		return valueClass;
	}
	
	public final boolean hasRegister(K key) {
		if (key == null) {
			return false;
		}
		return registry.containsKey(key);
	}
	public final boolean hasValue(V data) {
		return reverseRegistry.containsKey(new Unit<V>(data));
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
	private final ExpirationListener<K, Unit<V>> onExpiration = new ExpirationListener<K, Unit<V>>() {
		public void expired(K key, Unit<V> value) {
			reverseRegistry.remove(value);
			keysDirty.set(true);
			expire.invoke(this, new ExpireEventArgs<K, V>(key, value.getType()));
		}
	};
}
