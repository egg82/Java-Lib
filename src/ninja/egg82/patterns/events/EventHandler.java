package ninja.egg82.patterns.events;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ninja.egg82.exceptions.ArgumentNullException;

public class EventHandler<T extends EventArgs> {
	//vars
	private static final Logger logger = Logger.getLogger("ninja.egg82.patterns.events.EventHandler");
	
	private ArrayList<BiConsumer<Object, T>> listeners = new ArrayList<BiConsumer<Object, T>>();
	
	//constructor
	public EventHandler() {
		
	}
	
	//public
	public synchronized void attach(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new ArgumentNullException("listener");
		}
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	public synchronized void detatch(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new ArgumentNullException("listener");
		}
		listeners.remove(listener);
	}
	public synchronized void detatchAll() {
		listeners.clear();
	}
	
	public synchronized void invoke(Object sender, T args) {
		for (BiConsumer<Object, T> func : listeners) {
			try {
				func.accept(sender, args);
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Could not invoke listener.", ex);
				if (logger.isLoggable(Level.WARNING)) {
					System.out.println("Could not invoke listener.");
					ex.printStackTrace();
				}
			}
		}
	}
	
	public synchronized int numListeners() {
		return listeners.size();
	}
	
	//private
	
}
