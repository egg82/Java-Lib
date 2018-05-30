package ninja.egg82.patterns.events;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandler<T extends EventArgs> {
	//vars
	private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	
	private Deque<BiConsumer<Object, T>> listeners = new ConcurrentLinkedDeque<BiConsumer<Object, T>>();
	private Deque<BiConsumer<Object, T>> onceListeners = new ConcurrentLinkedDeque<BiConsumer<Object, T>>();
	
	//constructor
	public EventHandler() {
		
	}
	
	//public
	public void attach(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	public void attachOnce(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		if (onceListeners.contains(listener)) {
			return;
		}
		onceListeners.add(listener);
	}
	
	public void detatch(BiConsumer<Object, T> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		listeners.remove(listener);
	}
	public void detatchAll() {
		listeners.clear();
	}
	
	public T invoke(Object sender, T args) {
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
		return args;
	}
	
	public int numListeners() {
		return listeners.size();
	}
	
	//private
	
}
