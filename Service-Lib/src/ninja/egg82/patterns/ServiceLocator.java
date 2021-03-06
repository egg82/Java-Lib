package ninja.egg82.patterns;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import ninja.egg82.core.CollectionUtil;
import ninja.egg82.core.ServiceReflectUtil;

public final class ServiceLocator {
	//vars
	private static Deque<Class<?>> services = new ConcurrentLinkedDeque<Class<?>>();
	private static ConcurrentHashMap<Class<?>, Object> initializedServices = new ConcurrentHashMap<Class<?>, Object>();
	private static Cache<Class<?>, Object> lookupCache = Caffeine.newBuilder().build();
	
	//constructor
	public ServiceLocator() {
		
	}
	
	//public
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		Object result = initializedServices.get(clazz);
		
		if (result == null) {
			if (services.contains(clazz)) {
				result = CollectionUtil.putIfAbsent(initializedServices, clazz, initializeService(clazz));
			}
		}
		
		if (result == null) {
			result = lookupCache.getIfPresent(clazz);
		}
		
		if (result == null) {
			for (Class<?> c : services) {
				if (ServiceReflectUtil.doesExtend(clazz, c)) {
					result = initializedServices.get(c);
					if (result == null) {
						result = CollectionUtil.putIfAbsent(initializedServices, c, initializeService(c));
					}
					lookupCache.put(clazz, result);
					break;
				}
			}
		}
		
		if (result == null) {
			return null;
		}
		
		return (T) result;
	}
	public static void provideService(Class<?> clazz) {
		provideService(clazz, true);
	}
	public static void provideService(Class<?> clazz, boolean lazyInitialize) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		// Destroy any existing services and cache
		initializedServices.remove(clazz);
		lookupCache.asMap().keySet().removeIf(new Predicate<Class<?>>() {
			public boolean test(Class<?> t) {
				return ServiceReflectUtil.doesExtend(t, clazz);
			}
		});
		
		if (!lazyInitialize) {
			initializedServices.put(clazz, initializeService(clazz));
		}
		
		if (!services.contains(clazz)) {
			services.add(clazz);
		}
	}
	public static void provideService(Object initializedService) {
		if (initializedService == null) {
			throw new IllegalArgumentException("initializedService cannot be null.");
		}
		
		Class<?> clazz = initializedService.getClass();
		
		// Destroy any existing services and cache
		lookupCache.asMap().keySet().removeIf(new Predicate<Class<?>>() {
			public boolean test(Class<?> t) {
				return ServiceReflectUtil.doesExtend(t, clazz);
			}
		});
		initializedServices.put(clazz, initializedService);
		
		if (!services.contains(clazz)) {
			services.add(clazz);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T> removeServices(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		ArrayList<T> retVal = new ArrayList<T>();
		
		lookupCache.asMap().keySet().removeIf(new Predicate<Class<?>>() {
			public boolean test(Class<?> t) {
				return ServiceReflectUtil.doesExtend(t, clazz);
			}
		});
		
		for (Class<?> c : services) {
			if (ServiceReflectUtil.doesExtend(clazz, c)) {
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
		
		boolean result = services.contains(clazz);
		
		if (!result) {
			for (Class<?> c : services) {
				if (ServiceReflectUtil.doesExtend(clazz, c)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	public static boolean serviceIsInitialized(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		
		boolean result = initializedServices.containsKey(clazz);
		
		if (!result) {
			for (Class<?> c : initializedServices.keySet()) {
				if (ServiceReflectUtil.doesExtend(clazz, c)) {
					result = true;
					break;
				}
			}
		}
		
		return result;
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
