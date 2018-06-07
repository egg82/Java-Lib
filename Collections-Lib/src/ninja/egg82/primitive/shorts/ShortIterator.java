package ninja.egg82.primitive.shorts;

import java.util.Iterator;

public interface ShortIterator extends Iterator<Short> {
	//functions
	short nextShort();
	int skip(int n);
}
