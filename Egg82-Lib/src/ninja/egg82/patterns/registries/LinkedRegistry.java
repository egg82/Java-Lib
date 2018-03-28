package ninja.egg82.patterns.registries;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.tuples.Unit;

public class LinkedRegistry<K, V> implements IRegistry<K, V> {
	//vars
	private Class<K> keyClass = null;
	private Class<V> valueClass = null;
	private K[] keyCache = null;
	private AtomicBoolean keysDirty = new AtomicBoolean(false);
	private ConcurrentLinkedHashMap<K, Unit<V>> registry = new ConcurrentLinkedHashMap.Builder<K, Unit<V>>().maximumWeightedCapacity(Integer.MAX_VALUE).build();
	private ConcurrentMap<Unit<V>, K> reverseRegistry = new ConcurrentLinkedHashMap.Builder<Unit<V>, K>().maximumWeightedCapacity(Integer.MAX_VALUE).build();
	
	//constructor
	@SuppressWarnings("unchecked")
	public LinkedRegistry(Class<K> keyClass, Class<V> valueClass) {
		this.keyClass = keyClass;
		this.valueClass = valueClass;
		keyCache = (K[]) Array.newInstance(keyClass, 0);
	}
	@SuppressWarnings("unchecked")
	public LinkedRegistry(K[] keyArray, V[] valueArray) {
		this.keyClass = (Class<K>) keyArray.getClass().getComponentType();
		this.valueClass = (Class<V>) valueArray.getClass().getComponentType();
		keyCache = (K[]) Array.newInstance(keyClass, 0);
	}
	
	//public
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
			keyCache = registry.ascendingKeySet().toArray((K[]) Array.newInstance(keyClass, 0));
		}
		return keyCache.clone();
	}
	
	//private
	
}
