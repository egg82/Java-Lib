package ninja.egg82.patterns;

import java.util.ArrayList;
import java.util.HashMap;

import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.utils.ReflectUtil;

public final class ServiceLocator {
	//vars
	private static ArrayList<Class<?>> services = new ArrayList<Class<?>>();
	private static HashMap<Class<?>, Object> initializedServices = new HashMap<Class<?>, Object>();
	private static HashMap<Class<?>, Object> lookupCache = new HashMap<Class<?>, Object>();
	
	//constructor
	public ServiceLocator() {
		
	}
	
	//public
	@SuppressWarnings("unchecked")
	public synchronized static <T> T getService(Class<T> clazz) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		Object result = initializedServices.get(clazz);
		
		if (result == null) {
			int index = services.indexOf(clazz);
			if (index > -1) {
				result = initializeService(services.get(index));
				initializedServices.put(clazz, result);
			}
		}
		
		if (result == null) {
			result = lookupCache.get(clazz);
		}
		
		if (result == null) {
			for (int i = 0; i < services.size(); i++) {
				Class<?> c = services.get(i);
				if (ReflectUtil.doesExtend(clazz, c)) {
					result = initializedServices.get(c);
					if (result == null) {
						result = initializeService(c);
						initializedServices.put(clazz, result);
					}
					lookupCache.put(clazz, result);
					break;
				}
			}
		}
		
		return (T) result;
	}
	public synchronized static void provideService(Class<?> clazz) {
		provideService(clazz, true);
	}
	public synchronized static void provideService(Class<?> clazz, boolean lazyInitialize) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		// Destroy any existing services and cache
		initializedServices.remove(clazz);
		lookupCache.entrySet().removeIf(v -> ReflectUtil.doesExtend(clazz, v.getValue().getClass()));
		
		int index = services.indexOf(clazz);
		if (index > -1) {
			services.set(index, clazz);
		} else {
			services.add(clazz);
		}
		
		if (!lazyInitialize) {
			initializedServices.put(clazz, initializeService(clazz));
		}
	}
	public synchronized static void removeService(Class<?> clazz) {
		if (clazz == null) {
			throw new ArgumentNullException("clazz");
		}
		
		Object result = initializedServices.get(clazz);
		if (result != null) {
			initializedServices.remove(clazz);
		}
		services.remove(clazz);
		
		if (result == null) {
			for (int i = 0; i < services.size(); i++) {
				Class<?> c = services.get(i);
				if (ReflectUtil.doesExtend(clazz, c)) {
					result = initializedServices.get(c);
					if (result != null) {
						initializedServices.remove(clazz);
					}
					lookupCache.entrySet().removeIf(v -> ReflectUtil.doesExtend(clazz, v.getValue().getClass()));
					services.remove(i);
					return;
				}
			}
		}
	}
	
	public synchronized static boolean hasService(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}
		return services.contains(clazz);
	}
	public synchronized static boolean serviceIsInitialized(Class<?> clazz) {
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
