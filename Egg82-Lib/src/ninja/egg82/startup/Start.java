package ninja.egg82.startup;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.utils.ReflectUtil;

public final class Start {
    //vars
	private static Logger logger = LoggerFactory.getLogger(Start.class);
    
    //constructor
    public Start() {
        
    }
    
    //public
    @SuppressWarnings("resource")
	public static void init() {
    	URLClassLoader loader = null;
    	
    	if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
    		loader = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        	
        	try {
        		Field scl = ClassLoader.class.getDeclaredField("scl");
            	scl.setAccessible(true);
            	scl.set(null, loader);
        	} catch (Exception ex) {
        		logger.warn("Could not set system class loader.", ex);
        	}
    	} else {
    		loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    	}
    	
    	ReflectUtil.loadClasses(
    		"http://central.maven.org/maven2/com/github/ben-manes/caffeine/caffeine/2.6.2/caffeine-2.6.2.jar",
    		"caffeine-2.6.2.jar",
    		//"com.github.benmanes.caffeine", "ninja.egg82.lib.com.github.benmanes.caffeine",
    		loader
    	);
    	ReflectUtil.loadClasses(
    		"http://central.maven.org/maven2/it/unimi/dsi/fastutil/8.1.1/fastutil-8.1.1.jar",
    		"fastutil-8.1.1.jar",
    		//"it.unimi.dsi.fastutil", "ninja.egg82.lib.it.unimi.dsi.fastutil",
    		loader
    	);
    	
        ServiceLocator.provideService(InitRegistry.class, false);
    }
    
    //private
    
}
