package ninja.egg82.startup;

import ninja.egg82.patterns.Registry;

public final class InitRegistry extends Registry<String> {
	//vars
	
	//constructor
	public InitRegistry() {
		super(String.class);
		
		setRegister("java.version", System.getProperty("java.version"));
	}
	
	//public
	
	//private
	
}
