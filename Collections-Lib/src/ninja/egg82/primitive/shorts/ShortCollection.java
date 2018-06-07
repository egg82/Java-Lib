package ninja.egg82.primitive.shorts;

import java.util.Collection;

public interface ShortCollection extends Collection<Short>, ShortIterable {
	//functions
	ShortIterator iterator();
	ShortIterator shortIterator();
	
	<T> T[] toArray(T[] a);
	
	boolean contains(short key);
	
	short[] toShortArray();
	short[] toShortArray(short a[]);
	short[] toArray(short a[]);
	
	boolean add(short key);
	boolean rem(short key);
	
	boolean addAll(ShortCollection c);
	boolean containsAll(ShortCollection c);
	boolean removeAll(ShortCollection c);
	boolean retainAll(ShortCollection c);
}
