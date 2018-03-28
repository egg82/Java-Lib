package ninja.egg82.patterns;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.FixedConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;

public class DoubleBuffer<T> {
	//vars
	private volatile IConcurrentDeque<T> currentBuffer = null;
	private volatile IConcurrentDeque<T> backBuffer = null;
	private volatile ReadWriteLock lock = new ReentrantReadWriteLock();
	
	//constructor
	public DoubleBuffer() {
		currentBuffer = new DynamicConcurrentDeque<T>();
		backBuffer = new DynamicConcurrentDeque<T>();
	}
	public DoubleBuffer(int fixedSize) {
		currentBuffer = new FixedConcurrentDeque<T>(fixedSize);
		backBuffer = new FixedConcurrentDeque<T>(fixedSize);
	}
	
	//public
	public IConcurrentDeque<T> getCurrentBuffer() {
		lock.readLock().lock();
		IConcurrentDeque<T> t = currentBuffer;
		lock.readLock().unlock();
		return t;
	}
	public IConcurrentDeque<T> getBackBuffer() {
		lock.readLock().lock();
		IConcurrentDeque<T> t = backBuffer;
		lock.readLock().unlock();
		return t;
	}
	
	public void swapBuffers() {
		lock.writeLock().lock();
		IConcurrentDeque<T> t = currentBuffer;
		currentBuffer = backBuffer;
		backBuffer = t;
		lock.writeLock().unlock();
	}
	
	//private
	
}
