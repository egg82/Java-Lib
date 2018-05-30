package ninja.egg82.patterns.registries;

import java.util.concurrent.TimeUnit;

import ninja.egg82.events.VariableRegisterExpireEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface IVariableExpiringRegistry<K> extends IVariableRegistry<K> {
	//functions
	EventHandler<VariableRegisterExpireEventArgs<K>> onExpire();
	
	void setRegister(K key, Object data, long expirationTime, TimeUnit expirationUnit);
	void setRegister(K key, Object data, long expirationTime, TimeUnit expirationUnit, ninja.egg82.enums.ExpirationPolicy policy);
	void setRegisterExpiration(K key, long expirationTime, TimeUnit expirationUnit);
	void setRegisterPolicy(K key, ninja.egg82.enums.ExpirationPolicy policy);
	long getExpirationTime(K key);
	long getTimeRemaining(K key);
	void resetExpirationTime(K key);
}
