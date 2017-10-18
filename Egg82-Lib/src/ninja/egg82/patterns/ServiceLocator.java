package ninja.egg82.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.utils.CollectionUtil;
import ninja.egg82.utils.ReflectUtil;

public final class ServiceLocator {
	//vars
	private static IObjectPool<Class<?>> services = new DynamicObjectPool<Class<?>>();
	private static ConcurrentHashMap<Class<?>, Object> initializedServices = new ConcurrentHashMap<Class<?>, Object>();
	private static ConcurrentHashMap<Class<?>, Object> lookupCache = new ConcurrentHashMap<Class<?>, Object>();
	
	//constructor
	public ServiceLocator() {
		
	}
	
	//public
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		Object result = initializedServices.get(clazz);
		
		if (result == null) {
			if (services.contains(clazz)) {
				result = CollectionUtil.putIfAbsent(initializedServices, clazz, initializeService(clazz));
			}
		}
		
		if (result == null) {
			result = lookupCache.get(clazz);
		}
		
		if (result == null) {
			for (Class<?> c : services) {
				if (ReflectUtil.doesExtend(clazz, c)) {
					result = initializedServices.get(c);
					if (result == null) {
						result = CollectionUtil.putIfAbsent(initializedServices, c, initializeService(c));
					}
					lookupCache.putIfAbsent(clazz, result);
					break;
				}
			}
		}
		
		if (result == null) {
			return null;
		} else {
			return (T) result;
		}
	}
	public static void provideService(Class<?> clazz) {
		provideService(clazz, true);
	}
	public static void provideService(Class<?> clazz, boolean lazyInitialize) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		// Destroy any existing services and cache
		initializedServices.remove(clazz);
		lookupCache.entrySet().removeIf(v -> ReflectUtil.doesExtend(v.getKey(), clazz));
		
		if (!lazyInitialize) {
			initializedServices.put(clazz, initializeService(clazz));
		}
		
		if (!services.contains(clazz)) {
			services.add(clazz);
		}
	}
	public static void provideService(Object initializedService) {
		if (initializedService == null) {
			throw new ArgumentNullException("initializedService");
		}
		
		Class<?> clazz = initializedService.getClass();
		
		// Destroy any existing services and cache
		lookupCache.entrySet().removeIf(v -> ReflectUtil.doesExtend(v.getKey(), clazz));
		initializedServices.put(clazz, initializedService);
		
		if (!services.contains(clazz)) {
			services.add(clazz);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T> removeServices(Class<T> clazz) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		ArrayList<T> retVal = new ArrayList<T>();
		
		lookupCache.entrySet().removeIf(v -> ReflectUtil.doesExtend(v.getKey(), clazz));
		
		for (Class<?> c : services) {
			if (ReflectUtil.doesExtend(clazz, c)) {
				services.remove(c);
				T result = (T) initializedServices.remove(c);
				if (result != null) {
					retVal.add(result);
				}
			}
		}
		
		return retVal;
	}
	
	public static boolean hasService(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		return services.contains(clazz);
	}
	public static boolean serviceIsInitialized(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		return initializedServices.containsKey(clazz);
	}
	
	//private
	private static Object initializeService(Class<?> service) {
		Object instance = null;
		
		try {
			instance = service.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Service cannot be initialized.", ex);
		}
		
		return instance;
	}
}
