package ninja.egg82.patterns;

import java.util.Collection;

public interface IObjectPool<T> extends Collection<T> {
	//functions
	int remainingCapacity();
	int capacity();
	
	T popFirst();
	T peekFirst();
	T popLast();
	T peekLast();
	
	/**
     * Ensures that this collection contains the specified element at the
     * beginning of the collection (optional operation).  Returns <tt>true</tt>
     * if this collection changed as a result of the call.  (Returns <tt>false</tt>
     * if this collection does not permit duplicates and already contains the
     * specified element.)<p>
     *
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     *
     * If a collection refuses to add a particular element for any reason
     * other than that it already contains the element, it <i>must</i> throw
     * an exception (rather than returning <tt>false</tt>).  This preserves
     * the invariant that a collection always contains the specified element
     * after this call returns.
     *
     * @param e element whose presence in this collection is to be ensured
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *         is not supported by this collection
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this collection
     * @throws NullPointerException if the specified element is null and this
     *         collection does not permit null elements
     * @throws IllegalArgumentException if some property of the element
     *         prevents it from being added to this collection
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to insertion restrictions
     */
	boolean addFirst(T e);
}
