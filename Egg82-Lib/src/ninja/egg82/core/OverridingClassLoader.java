package ninja.egg82.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverridingClassLoader extends URLClassLoader {
	//vars
	private ClassLoader parent = null;
	
	private static Method FIND_RESOURCE = null;
	private static Method FIND_RESOURCES = null;
	private static Method FIND_CLASS = null;
	private static Method GET_PACKAGE = null;
	private static Method GET_PACKAGES = null;
	private static Method FIND_LIBRARY = null;
	
	private static Logger logger = LoggerFactory.getLogger(OverridingClassLoader.class);
	
	static {
		try {
			FIND_RESOURCE = ClassLoader.class.getDeclaredMethod("findResource", String.class);
			FIND_RESOURCE.setAccessible(true);
			FIND_RESOURCES = ClassLoader.class.getDeclaredMethod("findResources", String.class);
			FIND_RESOURCES.setAccessible(true);
			FIND_CLASS = ClassLoader.class.getDeclaredMethod("findClass", String.class);
			FIND_CLASS.setAccessible(true);
			GET_PACKAGE = ClassLoader.class.getDeclaredMethod("getPackage", String.class);
			GET_PACKAGE.setAccessible(true);
			GET_PACKAGES = ClassLoader.class.getDeclaredMethod("getPackages");
			GET_PACKAGES.setAccessible(true);
			FIND_LIBRARY = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
			FIND_LIBRARY.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException("Could not get method handlerd for class loader.", ex);
		}
	}
	
	//constructor
	public OverridingClassLoader(ClassLoader parent) {
		this(new URL[0], parent);
	}
	public OverridingClassLoader(URL[] urls, ClassLoader parent) {
		this(urls, parent, null);
	}
	public OverridingClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, ClassLoader.getSystemClassLoader(), factory);
		this.parent = parent;
	}
	
	//public
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> retVal = null;
		try {
			retVal = super.loadClass(name);
		} catch (ClassNotFoundException ex) {
			if (parent != null) {
				return parent.loadClass(name);
			}
		}
		return retVal;
	}
	
	public URL findResource(final String name) {
		URL retVal = super.findResource(name);
		if (retVal == null && parent != null) {
			try {
				return (URL) FIND_RESOURCE.invoke(parent, name);
			} catch (Exception ex) {
				logger.warn("Could not invoke method.", ex);
			}
		}
		return retVal;
	}
	@SuppressWarnings("unchecked")
	public Enumeration<URL> findResources(final String name) throws IOException {
		Set<URL> retVal = new HashSet<URL>();
		
		Enumeration<URL> t = null;
		try {
			t = super.findResources(name);
			while (t.hasMoreElements()) {
				retVal.add(t.nextElement());
			}
		} catch (IOException ex) {
			
		}
		if (parent != null) {
			try {
				t = (Enumeration<URL>) FIND_RESOURCES.invoke(parent, name);
				while (t.hasMoreElements()) {
					retVal.add(t.nextElement());
				}
			} catch (Exception ex) {
				logger.warn("Could not invoke method.", ex);
			}
		}
		
		return new Vector<URL>(retVal).elements();
	}
	
	public URL getResource(String name) {
		URL retVal = super.getResource(name);
		if (retVal == null && parent != null) {
			return parent.getResource(name);
		}
		return retVal;
	}
	public Enumeration<URL> getResources(String name) throws IOException {
		Set<URL> retVal = new HashSet<URL>();
		
		Enumeration<URL> t = null;
		try {
			t = super.getResources(name);
			while (t.hasMoreElements()) {
				retVal.add(t.nextElement());
			}
		} catch (IOException ex) {
			
		}
		if (parent != null) {
			try {
				t = parent.getResources(name);
				while (t.hasMoreElements()) {
					retVal.add(t.nextElement());
				}
			} catch (IOException ex) {
				
			}
		}
		
		return new Vector<URL>(retVal).elements();
	}
	
	public InputStream getResourceAsStream(String name) {
		InputStream retVal = super.getResourceAsStream(name);
		if (retVal == null && parent != null) {
			return parent.getResourceAsStream(name);
		}
		return retVal;
	}
	
	//private
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> retVal = null;
		try {
			retVal = super.findClass(name);
		} catch (ClassNotFoundException ex) {
			if (parent != null) {
				try {
					return (Class<?>) FIND_CLASS.invoke(parent, name);
				} catch (Exception ex2) {
					logger.warn("Could not invoke method.", ex2);
				}
			}
		}
		return retVal;
	}
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> retVal = null;
		try {
			retVal = super.loadClass(name);
		} catch (ClassNotFoundException ex) {
			if (parent != null) {
				return parent.loadClass(name);
			}
		}
		return retVal;
	}
	
	protected Package getPackage(String name) {
		Package retVal = super.getPackage(name);
		
		if (retVal == null && parent != null) {
			try {
				return (Package) GET_PACKAGE.invoke(parent, name);
			} catch (Exception ex2) {
				logger.warn("Could not invoke method.", ex2);
			}
		}
		
		return retVal;
	}
	protected Package[] getPackages() {
		Set<Package> retVal = new HashSet<Package>();
		
		Package[] t = super.getPackages();
		for (Package p : t) {
			retVal.add(p);
		}
		if (parent != null) {
			try {
				t = (Package[]) GET_PACKAGES.invoke(parent);
				for (Package p : t) {
					retVal.add(p);
				}
			} catch (Exception ex2) {
				logger.warn("Could not invoke method.", ex2);
			}
		}
		
		return retVal.toArray(new Package[0]);
	}
	
	protected String findLibrary(String libname) {
		String retVal = super.findLibrary(libname);
		if (retVal == null && parent != null) {
			try {
				return (String) FIND_LIBRARY.invoke(parent, libname);
			} catch (Exception ex) {
				logger.warn("Could not invoke method.", ex);
			}
		}
		return retVal;
	}
}
