package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class CompleteEventArgs<T> extends EventArgs {
	//vars
	public static final CompleteEventArgs<Object> EMPTY = new CompleteEventArgs<Object>(null);
	private T data = null;
	
	//constructor
	public CompleteEventArgs(T data) {
		super();
		
		this.data = data;
	}
	
	//public
	public T getData() {
		return data;
	}
	
	//private
	
}
