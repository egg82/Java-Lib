package ninja.egg82.primitive.shorts;

import java.util.ListIterator;

public interface ShortListIterator extends ListIterator<Short>, ShortBidirectionalIterator {
	//functions
	void set(short k);
	void add(short k);
}
