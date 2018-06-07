package ninja.egg82.utils;

public class SimpleReflectUtil {
	//vars
	
	//constructor
	public SimpleReflectUtil() {
		
	}
	
	//public
	public static boolean doesExtend(Class<?> baseClass, Class<?> classToTest) {
		if (classToTest == null || baseClass == null) {
			return false;
		}
		
		return classToTest.equals(baseClass) || baseClass.isAssignableFrom(classToTest);
	}
	
	//private
	
}
