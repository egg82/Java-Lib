package ninja.egg82.patterns.prototypes;

public interface IPrototypeFactory {
	//functions
    void addPrototype(String name, IPrototype prototype);
    void removePrototype(String name);
    boolean hasPrototype(String name);
    IPrototype createInstance(String name);
    IPrototype[] createInstances(String name, int numInstances);
}
