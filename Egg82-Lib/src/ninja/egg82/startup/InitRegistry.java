package ninja.egg82.startup;

import ninja.egg82.patterns.registries.VariableRegistry;

public final class InitRegistry extends VariableRegistry<String> {
	//vars
	
	//constructor
	public InitRegistry() {
		super(String.class);
		
		setRegister("java.version", System.getProperty("java.version"));
	}
	
	//public
	
	//private
	
}
