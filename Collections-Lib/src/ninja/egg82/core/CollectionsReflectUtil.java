package ninja.egg82.core;

public class CollectionsReflectUtil {
	//vars
	
	//constructor
	public CollectionsReflectUtil() {
		
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
