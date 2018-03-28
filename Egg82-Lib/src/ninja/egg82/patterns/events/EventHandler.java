package ninja.egg82.patterns.events;

import java.util.Iterator;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;
import ninja.egg82.exceptions.ArgumentNullException;

public class EventHandler<T extends EventArgs> {
	//vars
	private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	
	private IConcurrentDeque<BiConsumer<Object, T>> listeners = new DynamicConcurrentDeque<BiConsumer<Object, T>>();
	private IConcurrentDeque<BiConsumer<Object, T>> onceListeners = new DynamicConcurrentDeque<BiConsumer<Object, T>>();
	
	//constructor
	public EventHandler() {
		
	}
	
	//public
	public void attach(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new ArgumentNullException("listener");
		}
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	public void attachOnce(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new ArgumentNullException("listener");
		}
		if (onceListeners.contains(listener)) {
			return;
		}
		onceListeners.add(listener);
	}
	
	public void detatch(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new ArgumentNullException("listener");
		}
		listeners.remove(listener);
	}
	public void detatchAll() {
		listeners.clear();
	}
	
	public void invoke(Object sender, T args) {
		for (Iterator<BiConsumer<Object, T>> i = onceListeners.iterator(); i.hasNext();) {
			BiConsumer<Object, T> func = i.next();
			try {
				func.accept(sender, args);
			} catch (Exception ex) {
				logger.warn("Could not invoke listener.", ex);
			}
			i.remove();
		}
		for (BiConsumer<Object, T> func : listeners) {
			try {
				func.accept(sender, args);
			} catch (Exception ex) {
				logger.warn("Could not invoke listener.", ex);
			}
		}
	}
	
	public int numListeners() {
		return listeners.size();
	}
	
	//private
	
}
