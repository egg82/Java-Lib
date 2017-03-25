package ninja.egg82.patterns;

public interface IRegistry {
	//functions
	void setRegister(String name, Class<?> type, Object data);
	Object getRegister(String name);
	Class<?> getRegisterClass(String name);
	boolean hasRegister(String name);
	
	void clear();
	String[] getRegistryNames();
}
