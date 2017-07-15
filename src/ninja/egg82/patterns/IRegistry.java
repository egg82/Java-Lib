package ninja.egg82.patterns;

public interface IRegistry {
	//functions
	void setRegister(String name, Class<?> type, Object data);
	void removeRegister(String name);
	
	Object getRegister(String name);
	<T> T getRegister(String name, Class<T> type);
	String getName(Object data);
	Class<?> getRegisterClass(String name);
	
	boolean hasRegister(String name);
	boolean hasValue(Object data);
	
	void clear();
	String[] getRegistryNames();
}
