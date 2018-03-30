package ninja.egg82.startup;

import ninja.egg82.patterns.ServiceLocator;

public final class Start {
    //vars
	
    //constructor
    public Start() {
        
    }
    
    //public
	public static void init() {
        ServiceLocator.provideService(InitRegistry.class, false);
    }
    
    //private
    
}
