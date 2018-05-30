package ninja.egg82.concurrent;

import java.util.Queue;

public interface IConcurrentQueue<T> extends Queue<T> {
	//functions
	int getRemainingCapacity();
	int getCapacity();
}
