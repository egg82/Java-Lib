package ninja.egg82.startup;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.prototypes.PrototypeFactory;

public class Start {
    //vars
    
    //constructor
    public Start() {
        
    }
    
    //public
    public static void init() {
        ServiceLocator.provideService(PrototypeFactory.class);
    }
    
    //private
    
}
