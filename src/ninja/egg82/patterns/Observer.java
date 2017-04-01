package ninja.egg82.patterns;

import java.util.ArrayList;
import java.util.List;

public final class Observer {
	//vars
	private ArrayList<TriFunction<Object, String, Object, Void>> listeners = new ArrayList<TriFunction<Object, String, Object, Void>>();
	
	//constructor
	public Observer() {
	    
	}
	
	//public
	public static void add(List<Observer> list, Observer observer) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null.");
		}
		if (observer == null || list.contains(observer)) {
			return;
		}
		list.add(observer);
	}
	public static void remove(List<Observer> list, Observer observer) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null.");
		}
		if (observer == null) {
			return;
		}
		list.remove(observer);
	}
	
	public static void dispatch(List<Observer> list, Object sender, String event) {
		dispatch(list, sender, event, null);
	}
	public static void dispatch(List<Observer> list, Object sender, String event, Object data) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null.");
		}
		if (list.isEmpty()) {
			return;
		}
		for (Observer observer : list) {
			observer.dispatch(sender, event, data);
		}
	}
	
	public synchronized void add(TriFunction<Object, String, Object, Void> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);
	}
	public synchronized void remove(TriFunction<Object, String, Object, Void> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener cannot be null.");
		}
		listeners.remove(listener);
	}
	public synchronized void removeAll() {
		listeners.clear();
	}
	
	public synchronized void dispatch(Object sender, String event, Object data) {
		for (TriFunction<Object, String, Object, Void> func : listeners) {
			try {
				func.apply(sender, event, data);
			} catch (Exception ex) {
				
			}
		}
	}
	
	public synchronized int numListeners() {
		return listeners.size();
	}
	
	//private

}
