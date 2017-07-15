package ninja.egg82.events;

import ninja.egg82.patterns.events.EventArgs;

public class ExceptionEventArgs extends EventArgs {
	//vars
	public static final ExceptionEventArgs EMPTY = new ExceptionEventArgs(null);
	private Exception ex = null;
	
	//constructor
	public ExceptionEventArgs(Exception ex) {
		super();
		
		this.ex = ex;
	}
	
	//public
	public Exception getException() {
		return ex;
	}
	
	//private
	
}
