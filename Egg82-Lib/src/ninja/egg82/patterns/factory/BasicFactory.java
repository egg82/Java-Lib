package ninja.egg82.patterns.factory;

import ninja.egg82.events.FinalizeEventArgs;
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.FixedObjectPool;
import ninja.egg82.patterns.IObjectPool;

public abstract class BasicFactory<T extends FactoryObject> {
	//vars
	
	// The pool of objects available for use
	private IObjectPool<T> freePool = null;
	// Whether or not the freePool is fixed
	private boolean dynamic = false;
	
	// The master class to clone from
	private Class<T> master = null;
	
	//constructor
	public BasicFactory(Class<T> clazz) {
		this(clazz, 0);
	}
	public BasicFactory(Class<T> clazz, int maxCount) {
		// Store the master class
		master = clazz;
		
		if (maxCount <= 0) {
			// freePool is dynamic
			freePool = new DynamicObjectPool<T>();
			dynamic = true;
		} else {
			// freePool is fixed, prepopulate default values
			freePool = new FixedObjectPool<T>(maxCount);
			while (freePool.remainingCapacity() > 0) {
				freePool.add(createInstance());
			}
		}
	}
	
	//public
	public T getInstance() {
		// Grab the oldest object first
		T retVal = freePool.popFirst();
		
		if (retVal == null) {
			// No more left in the free pool. Create one if dynamic, else null
			retVal = (dynamic) ? createInstance() : null;
		}
		
		// Return the new value
		return retVal;
	}
	
	//private
	private T createInstance() {
		T instance = null;
		
		// Create the instance
		try {
			instance = master.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException("Instance cannot be initialized.", ex);
		}
		
		// Attach the finalization listener
		instance.onFinalize().attach((s, e) -> onFinalize(s, e));
		
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private void onFinalize(Object sender, FinalizeEventArgs e) {
		// Sender should always be T, since we explicitly listen to only the objects created in this factory
		T s = (T) sender;
		
		// Clear the object, hopefully freeing some memory
		s.clear();
		
		// Add the object back to the unused/free pool
		freePool.add(s);
	}
}
