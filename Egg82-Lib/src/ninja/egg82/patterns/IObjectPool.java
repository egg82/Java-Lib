package ninja.egg82.patterns;

import java.util.Collection;

public interface IObjectPool<T> extends Collection<T> {
	//functions
	int remainingCapacity();
	int capacity();
	
	T pop();
	T peek();
}
