package ninja.egg82.patterns.registries;

import java.util.concurrent.TimeUnit;

import ninja.egg82.events.RegisterExpireEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface IExpiringRegistry<K, V> extends IRegistry<K, V> {
	//functions
	EventHandler<RegisterExpireEventArgs<K, V>> onExpire();
	
	void setRegister(K key, V data, long expirationTime, TimeUnit expirationUnit);
	void setRegister(K key, V data, long expirationTime, TimeUnit expirationUnit, ninja.egg82.enums.ExpirationPolicy policy);
	void setRegisterExpiration(K key, long expirationTime, TimeUnit expirationUnit);
	void setRegisterPolicy(K key, ninja.egg82.enums.ExpirationPolicy policy);
	long getExpirationTime(K key);
	long getTimeRemaining(K key);
	void resetExpirationTime(K key);
}
