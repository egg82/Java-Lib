package ninja.egg82.concurrent;

import java.util.Set;

public interface IConcurrentSet<T> extends Set<T> {
	//functions
	int getRemainingCapacity();
	int getCapacity();
}
