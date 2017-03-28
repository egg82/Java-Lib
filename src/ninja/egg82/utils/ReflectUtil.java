package ninja.egg82.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
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

public final class ReflectUtil {
	//vars
	
	//constructor
	public ReflectUtil() {
	    
	}
	
	//public
	public static void invokeMethod(String method, Object obj) {
	    Method find = null;
	    
	    try {
	        find = obj.getClass().getMethod(method, (Class<?>[]) null);
	    } catch (Exception ex) {
	        
	    }
	    
	    if (find != null) {
	        try {
	            find.invoke(obj, (Object[]) null);
	        } catch (Exception ex) {
	            
	        }
	    }
	}
	public static Field getMethod(String method, Object obj) {
	    Field find;
	    
	    try {
	        find = obj.getClass().getDeclaredField(method);
	    } catch (Exception ex) {
	        return null;
	    }
	    
	    return find;
	}
	
	public static Object[] getStaticFields(Class<?> c) {
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
	public static <T> ArrayList<T> getClasses(T clazz, String pkg) {
		Reflections ref = new Reflections(new ConfigurationBuilder()
				.setScanners(new SubTypesScanner(false),
						new ResourcesScanner(),
						new TypeElementsScanner())
				.setUrls(ClasspathHelper.forPackage(pkg))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg))));
		Set<String> typeSet = ref.getStore().get("TypeElementsScanner").keySet();
		
		Set<Class<?>> set = Sets.newHashSet(ReflectionUtils.forNames(typeSet, ref.getConfiguration().getClassLoaders()));
		ArrayList<T> list = new ArrayList<T>();
		
		Iterator<?> i = set.iterator();
		while (i.hasNext()) {
			list.add((T) i.next());
		}
		
		return list;
	}
	
	public static Class<?> getClassFromName(String name) {
		try {
			return Class.forName(name);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static boolean doesExtend(Class<?> baseClass, Class<?> classToTest) {
		return classToTest == baseClass || classToTest.isAssignableFrom(baseClass);
	}
	
	//private

}
