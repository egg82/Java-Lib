package ninja.egg82.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class EggReflectUtil {
	//vars
	
	//constructor
	public EggReflectUtil() {
		
	}
	
	//public
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
	
	//private
	
}
