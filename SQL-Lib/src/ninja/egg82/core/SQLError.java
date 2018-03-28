package ninja.egg82.core;

public class SQLError {
	//vars
	public volatile Exception ex = null;
	
	//constructor
	public SQLError() {
		
	}
	public SQLError(Exception ex) {
		this.ex = ex;
	}
	
	//public
	
	//private
	
}
