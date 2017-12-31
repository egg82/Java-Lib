package ninja.egg82.patterns;

import ninja.egg82.events.ExpireEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface IExpiringRegistry<K> extends IRegistry<K> {
	//functions
	EventHandler<ExpireEventArgs<K>> onExpire();
	
	void setRegister(K key, Object data, long expirationTimeMilliseconds);
	void setRegister(K key, Object data, long expirationTimeMilliseconds, ninja.egg82.enums.ExpirationPolicy policy);
	void setRegisterExpiration(K key, long expirationTimeMilliseconds);
	void setRegisterPolicy(K key, ninja.egg82.enums.ExpirationPolicy policy);
	long getExpirationTime(K key);
	long getTimeRemaining(K key);
	void resetExpirationTime(K key);
}
