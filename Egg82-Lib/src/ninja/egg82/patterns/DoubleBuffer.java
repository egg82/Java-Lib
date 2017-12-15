package ninja.egg82.patterns;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DoubleBuffer<T> {
	//vars
	private volatile IObjectPool<T> currentBuffer = null;
	private volatile IObjectPool<T> backBuffer = null;
	private volatile ReadWriteLock lock = new ReentrantReadWriteLock();
	
	//constructor
	public DoubleBuffer() {
		currentBuffer = new DynamicObjectPool<T>();
		backBuffer = new DynamicObjectPool<T>();
	}
	public DoubleBuffer(int fixedSize) {
		currentBuffer = new FixedObjectPool<T>(fixedSize);
		backBuffer = new FixedObjectPool<T>(fixedSize);
	}
	
	//public
	public IObjectPool<T> getCurrentBuffer() {
		lock.readLock().lock();
		IObjectPool<T> t = currentBuffer;
		lock.readLock().unlock();
		return t;
	}
	public IObjectPool<T> getBackBuffer() {
		lock.readLock().lock();
		IObjectPool<T> t = backBuffer;
		lock.readLock().unlock();
		return t;
	}
	
	public void swapBuffers() {
		lock.writeLock().lock();
		IObjectPool<T> t = currentBuffer;
		currentBuffer = backBuffer;
		backBuffer = t;
		lock.writeLock().unlock();
	}
	
	//private
	
}
