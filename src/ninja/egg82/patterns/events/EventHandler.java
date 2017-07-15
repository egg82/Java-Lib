package ninja.egg82.patterns.events;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class EventHandler<T extends EventArgs> {
	//vars
	private ArrayList<BiFunction<Object, T, Void>> listeners = new ArrayList<BiFunction<Object, T, Void>>();
	
	//constructor
	public EventHandler() {
		
	}
	
	//public
	public synchronized void attach(BiFunction<Object, T, Void> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	public synchronized void detatch(BiFunction<Object, T, Void> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		listeners.remove(listener);
	}
	public synchronized void detatchAll() {
		listeners.clear();
	}
	
	public synchronized void invoke(Object sender, T args) {
		for (BiFunction<Object, T, Void> func : listeners) {
			try {
				func.apply(sender, args);
			} catch (Exception ex) {
				throw new RuntimeException("Could not invoke listener", ex);
			}
		}
	}
	
	public synchronized int numListeners() {
		return listeners.size();
	}
	
	//private
	
}
