package ninja.egg82.filters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import ninja.egg82.core.EggReflectUtil;

public class EnumFilter<T> {
	//vars
	private T[] types = null;
	private Class<T> clazz = null;
	
	//constructor
	@SuppressWarnings("unchecked")
	public EnumFilter(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		this.clazz = clazz;
		
		Object[] enums = EggReflectUtil.getStaticFields(clazz);
		types = (T[]) Arrays.copyOf(enums, enums.length, ((T[]) Array.newInstance(clazz, 0)).getClass());
	}
	
	//public
	public T[] getAllTypes() {
		return types.clone();
	}
	@SuppressWarnings("unchecked")
	public T[] filter(T[] list, String filter, boolean whitelist) {
		if (list == null) {
			throw new IllegalArgumentException("list cannot be null.");
		}
		if (filter == null) {
			throw new IllegalArgumentException("filter cannot be null.");
		}
		
		filter = filter.toLowerCase();
		
		ArrayList<T> filteredTypes = new ArrayList<T>();
		
		for (T s : list) {
			String name = s.toString().toLowerCase();
			if (whitelist) {
				if (name.contains(filter)) {
					filteredTypes.add(s);
				}
			} else {
				if (!name.contains(filter)) {
					filteredTypes.add(s);
				}
			}
		}
		
		return filteredTypes.toArray((T[]) Array.newInstance(clazz, 0));
	}
	
	//private
	
}
