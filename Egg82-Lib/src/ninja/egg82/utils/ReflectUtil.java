package ninja.egg82.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import ninja.egg82.exceptions.ArgumentNullException;

public final class ReflectUtil {
	//vars
	
	//constructor
	public ReflectUtil() {
	    
	}
	
	//public
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
	
	public static Object[] getStaticFields(Class<?> c) {
		if (c == null) {
			throw new ArgumentNullException("c");
		}
		
		Field[] fields = c.getDeclaredFields();
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
			throw new ArgumentNullException("clazz");
		}
		if (pkg == null) {
			throw new ArgumentNullException("pkg");
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
			throw new ArgumentNullException("clazz");
		}
		if (pkg == null) {
			throw new ArgumentNullException("pkg");
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
