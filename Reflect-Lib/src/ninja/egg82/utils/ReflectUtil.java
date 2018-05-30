package ninja.egg82.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.collect.Sets;

import ninja.egg82.core.ReflectFileUtil;

public final class ReflectUtil {
	//vars
	private static Method m = null;
	
	static {
		try {
			m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			m.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException("Could not get method handler for dep injection.", ex);
		}
	}
	
	//constructor
	public ReflectUtil() {
	    
	}
	
	//public
	public static void loadClasses(String jarUrl, String jarFileName, URLClassLoader loader) {
		loadClasses(jarUrl, new File(new File("libs"), jarFileName), loader);
	}
	public static void loadClasses(String jarUrl, File jarFile, URLClassLoader loader) {
		// Make sure the directory and file structure is what we expect
		if (ReflectFileUtil.pathExists(jarFile) && !ReflectFileUtil.pathIsFile(jarFile)) {
			ReflectFileUtil.deleteDirectory(jarFile);
		}
		// If the file doesn't already exist, download it
		if (!ReflectFileUtil.pathExists(jarFile)) {
			URL url = null;
			try {
				File d = new File(jarFile.getParent());
	    		d.mkdirs();
				
	    		// Download the jar
				url = new URL(jarUrl);
				InputStream in = url.openStream();
				// Write the jar file to disk
				Files.copy(in, jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				// Cleanup
				in.close();
			} catch (Exception ex) {
				throw new RuntimeException("Could not download file.", ex);
			}
		}
		
		// Add downloaded jar into classpath
		try {
			m.invoke(loader, jarFile.toURI().toURL());
		} catch (Exception ex) {
			throw new RuntimeException("Could not load file into classpath.", ex);
		}
	}
	
	public static Object invokeMethod(String method, Object obj) {
		if (obj == null) {
			return null;
		}
		
	    Method find = null;
	    
	    try {
	        find = obj.getClass().getMethod(method, (Class<?>[]) null);
	    } catch (Exception ex) {
	    	return null;
	    }
	    
        try {
            return find.invoke(obj, (Object[]) null);
        } catch (Exception ex) {
        	return null;
        }
	}
	public static Object invokeMethod(String method, Object obj, Object... params) {
		if (obj == null) {
			return null;
		}
		
		if (params == null || params.length == 0) {
			return invokeMethod(method, obj);
		}
		
	    Method find = null;
	    
	    Class<?>[] classes = new Class<?>[params.length];
	    for (int i = 0; i < params.length; i++) {
	    	classes[i] = params[i].getClass();
	    }
	    
	    try {
	        find = obj.getClass().getMethod(method, classes);
	    } catch (Exception ex) {
	        return null;
	    }
	    
        try {
            return find.invoke(obj, params);
        } catch (Exception ex) {
        	return null;
        }
	}
	
	public static Field getField(String field, Object obj) {
		if (obj == null) {
			return null;
		}
		
		Field find;
	    
	    try {
	        find = obj.getClass().getDeclaredField(field);
	    } catch (Exception ex) {
	        return null;
	    }
	    
	    return find;
	}
	public static Method getMethod(String method, Object obj) {
		if (obj == null) {
			return null;
		}
		
		Method find;
	    
	    try {
	        find = obj.getClass().getDeclaredMethod(method);
	    } catch (Exception ex) {
	        return null;
	    }
	    
	    return find;
	}
	public static Method getMethod(String method, Class<?> c) {
		if (c == null) {
			return null;
		}
		
		Method find;
	    
	    try {
	        find = c.getDeclaredMethod(method);
	    } catch (Exception ex) {
	        return null;
	    }
	    
	    return find;
	}
	public static boolean hasMethod(String method, Object obj) {
		if (obj == null) {
			return false;
		}
	    
	    try {
	        obj.getClass().getDeclaredMethod(method);
	    } catch (Exception ex) {
	        return false;
	    }
	    
	    return true;
	}
	public static boolean hasMethod(String method, Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
	    
	    try {
	        clazz.getDeclaredMethod(method);
	    } catch (Exception ex) {
	        return false;
	    }
	    
	    return true;
	}
	
	public static Object[] getStaticFields(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		Field[] fields = clazz.getDeclaredFields();
		ArrayList<Object> returns = new ArrayList<Object>();
		
		for (int i = 0; i < fields.length; i++) {
			if (!Modifier.isPrivate(fields[i].getModifiers())) {
				try {
					returns.add(fields[i].get(null));
				} catch (Exception ex) {
					
				}
			}
		}
		
		return returns.toArray();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> getClasses(Class<T> clazz, String pkg, boolean recursive, boolean keepInterfaces, boolean keepAbstracts, String... excludePackages) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		if (pkg == null) {
			throw new IllegalArgumentException("pkg cannot be null.");
		}
		
		Reflections.log = null;
		
		String excludeString = null;
		if (excludePackages != null && excludePackages.length > 0) {
			for (int i = 0; i < excludePackages.length; i++) {
				excludePackages[i] = "-" + excludePackages[i];
			}
			excludeString = String.join(", ", excludePackages);
		}
		
		Reflections ref = null;
		try {
			ConfigurationBuilder config = new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false),
						new ResourcesScanner(),
						new TypeElementsScanner())
				.setUrls(ClasspathHelper.forPackage(pkg));
			
			if (excludeString != null) {
				config = config.filterInputsBy(FilterBuilder.parsePackages(excludeString).include(FilterBuilder.prefix(pkg)));
			} else {
				config = config.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg)));
			}
			
			ref = new Reflections(config);
		} catch (Exception ex) {
			return new ArrayList<Class<T>>();
		}
		
		Set<String> typeSet = ref.getStore().get("TypeElementsScanner").keySet();
		Set<Class<?>> set = Sets.newHashSet(ReflectionUtils.forNames(typeSet, ref.getConfiguration().getClassLoaders()));
		ArrayList<Class<T>> list = new ArrayList<Class<T>>();
		
		for (Class<?> next : set) {
			if (!keepInterfaces && next.isInterface()) {
				continue;
			}
			if (!keepAbstracts && Modifier.isAbstract(next.getModifiers())) {
				continue;
			}
			
			String n = next.getName();
			n = n.substring(n.indexOf('.') + 1);
			
			if (n.contains("$")) {
				continue;
			}
			
			if (!recursive) {
				String p = next.getName();
				p = p.substring(0, p.lastIndexOf('.'));
				
				if (!p.equalsIgnoreCase(pkg)) {
					continue;
				}
			}
			
			if (!ReflectUtil.doesExtend(clazz, next)) {
				continue;
			}
			
			list.add((Class<T>) next);
		}
		
		return list;
	}
	@SuppressWarnings("unchecked")
	public static <T> List<Class<? extends T>> getClassesParameterized(Class<T> clazz, String pkg, boolean recursive, boolean keepInterfaces, boolean keepAbstracts, String... excludePackages) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		if (pkg == null) {
			throw new IllegalArgumentException("pkg cannot be null.");
		}
		
		Reflections.log = null;
		
		String excludeString = null;
		if (excludePackages != null && excludePackages.length > 0) {
			for (int i = 0; i < excludePackages.length; i++) {
				excludePackages[i] = "-" + excludePackages[i];
			}
			excludeString = String.join(", ", excludePackages);
		}
		
		Reflections ref = null;
		try {
			ConfigurationBuilder config = new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false),
						new ResourcesScanner(),
						new TypeElementsScanner())
				.setUrls(ClasspathHelper.forPackage(pkg));
			
			if (excludeString != null) {
				config = config.filterInputsBy(FilterBuilder.parsePackages(excludeString).include(FilterBuilder.prefix(pkg)));
			} else {
				config = config.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg)));
			}
			
			ref = new Reflections(config);
		} catch (Exception ex) {
			return new ArrayList<Class<? extends T>>();
		}
		
		Set<String> typeSet = ref.getStore().get("TypeElementsScanner").keySet();
		Set<Class<?>> set = Sets.newHashSet(ReflectionUtils.forNames(typeSet, ref.getConfiguration().getClassLoaders()));
		ArrayList<Class<? extends T>> list = new ArrayList<Class<? extends T>>();
		
		for (Class<?> next : set) {
			if (!keepInterfaces && next.isInterface()) {
				continue;
			}
			if (!keepAbstracts && Modifier.isAbstract(next.getModifiers())) {
				continue;
			}
			
			String n = next.getName();
			n = n.substring(n.indexOf('.') + 1);
			
			if (n.contains("$")) {
				continue;
			}
			
			if (!recursive) {
				String p = next.getName();
				p = p.substring(0, p.lastIndexOf('.'));
				
				if (!p.equalsIgnoreCase(pkg)) {
					continue;
				}
			}
			
			if (!ReflectUtil.doesExtend(clazz, next)) {
				continue;
			}
			
			list.add((Class<? extends T>) next);
		}
		
		return list;
	}
	
	public static Class<?> getClassFromName(String name) {
		if (name == null) {
			return null;
		}
		
		try {
			return Class.forName(name);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static boolean doesExtend(Class<?> baseClass, Class<?> classToTest) {
		if (classToTest == null || baseClass == null) {
			return false;
		}
		
		return classToTest.equals(baseClass) || baseClass.isAssignableFrom(classToTest);
	}
	
	//private

}
