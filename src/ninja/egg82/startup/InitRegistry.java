package ninja.egg82.startup;

import ninja.egg82.patterns.Registry;

public final class InitRegistry extends Registry {
	//vars
	
	//constructor
	public InitRegistry() {
		setRegister("java.version", String.class, System.getProperty("java.version"));
	}
	
	//public
	
	//private
	
}
