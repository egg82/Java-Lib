package ninja.egg82.patterns;

import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import ninja.egg82.utils.ReflectUtil;

public final class ServiceLocator {
	//vars
	private static FastList<Class<?>> services = new FastList<Class<?>>();
	private static UnifiedMap<Class<?>, Object> initializedServices = new UnifiedMap<Class<?>, Object>();
	
	//constructor
	public ServiceLocator() {
		
	}
	
	//public
	public synchronized static Object getService(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		Object result = initializedServices.get(clazz);
		int index = services.indexOf(clazz);
		
		if (result == null && index > -1) {
			result = initializeService(services.get(index));
			initializedServices.put(clazz, result);
		}
		
		if (result == null) {
			for (int i = 0; i < services.size(); i++) {
				Class<?> c = services.get(i);
				if (ReflectUtil.doesExtend(c, clazz)) {
					result = initializedServices.get(c);
					if (result == null) {
						result = initializeService(c);
						initializedServices.put(clazz, result);
					}
					break;
				}
			}
		}
		
		return result;
	}
	public synchronized static void provideService(Class<?> clazz) {
		provideService(clazz, true);
	}
	public synchronized static void provideService(Class<?> clazz, boolean lazyInitialize) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		initializedServices.remove(clazz);
		
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
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		Object result = initializedServices.get(clazz);
		if (result != null) {
			initializedServices.remove(clazz);
		}
		services.remove(clazz);
		
		if (result == null) {
			for (int i = 0; i < services.size(); i++) {
				Class<?> c = services.get(i);
				if (ReflectUtil.doesExtend(c, clazz)) {
					result = initializedServices.get(c);
					if (result != null) {
						initializedServices.remove(clazz);
					}
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
