package ninja.egg82.patterns.factory;

import ninja.egg82.events.FinalizeEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public abstract class FactoryObject {
	//vars
	private EventHandler<FinalizeEventArgs> finalize = new EventHandler<FinalizeEventArgs>();
	
	//constructor
	public FactoryObject() {
		
	}
	public final void finalize() {
		finalize.invoke(this, FinalizeEventArgs.EMPTY);
	}
	
	//public
	public EventHandler<FinalizeEventArgs> onFinalize() {
		return finalize;
	}
	public abstract void clear();
	
	//private
	
}
