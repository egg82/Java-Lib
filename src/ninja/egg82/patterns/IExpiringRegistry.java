package ninja.egg82.patterns;

import ninja.egg82.events.ExpireEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface IExpiringRegistry<K> extends IRegistry<K> {
	//functions
	EventHandler<ExpireEventArgs<K>> onExpire();
}
