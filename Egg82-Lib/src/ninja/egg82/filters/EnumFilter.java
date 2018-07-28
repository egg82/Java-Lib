package ninja.egg82.filters;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EnumFilter<T> {
	//vars
	private Class<T> clazz = null;
	private List<T> currentTypes = null;
	
	//constructor
	@SuppressWarnings("unchecked")
	public EnumFilter(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz cannot be null.");
		}
		
		this.clazz = clazz;
		
		T[] enums = (clazz.isEnum()) ? clazz.getEnumConstants() : getStaticFields(clazz);
		currentTypes = new ArrayList<T>(Arrays.asList((T[]) Arrays.copyOf(enums, enums.length, ((T[]) Array.newInstance(clazz, 0)).getClass())));
	}
	
	//public
	public EnumFilter<T> whitelist(String filter) {
		if (filter == null) {
			throw new IllegalArgumentException("filter cannot be null.");
		}
		
		filter = filter.toLowerCase();
		
		for (Iterator<T> i = currentTypes.iterator(); i.hasNext();) {
			T s = i.next();
			String name = s.toString().toLowerCase();
			if (!name.contains(filter)) {
				i.remove();
			}
		}
		
		return this;
	}
	public EnumFilter<T> blacklist(String filter) {
		if (filter == null) {
			throw new IllegalArgumentException("filter cannot be null.");
		}
		
		filter = filter.toLowerCase();
		
		for (Iterator<T> i = currentTypes.iterator(); i.hasNext();) {
			T s = i.next();
			String name = s.toString().toLowerCase();
			if (name.contains(filter)) {
				i.remove();
			}
		}
		
		return this;
	}
	@SuppressWarnings("unchecked")
	public T[] build() {
		T[] retVal = (T[]) Array.newInstance(clazz, currentTypes.size());
		for (int i = 0; i < currentTypes.size(); i++) {
			retVal[i] = currentTypes.get(i);
		}
		
		return retVal;
	}
	
	//private
	@SuppressWarnings("unchecked")
	private T[] getStaticFields(Class<?> clazz) {
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
		
		return returns.toArray((T[]) Array.newInstance(clazz, 0));
	}
}
