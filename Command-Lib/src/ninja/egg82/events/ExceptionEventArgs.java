package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class ExceptionEventArgs<T extends Exception> extends EventArgs {
	//vars
	public static final ExceptionEventArgs<Exception> EMPTY = new ExceptionEventArgs<Exception>(null);
	private T ex = null;
	
	//constructor
	public ExceptionEventArgs(T ex) {
		super();
		
		this.ex = ex;
	}
	
	//public
	public T getException() {
		return ex;
	}
	
	//private
	
}
