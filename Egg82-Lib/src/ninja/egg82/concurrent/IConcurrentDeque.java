package ninja.egg82.concurrent;

import java.util.Deque;

public interface IConcurrentDeque<T> extends Deque<T> {
	//functions
	int getRemainingCapacity();
	int getCapacity();
}
