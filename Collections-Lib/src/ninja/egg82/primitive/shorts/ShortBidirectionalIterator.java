package ninja.egg82.primitive.shorts;

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public interface ShortBidirectionalIterator extends ShortIterator, ObjectBidirectionalIterator<Short> {
	//functions
	short previousShort();
	int back(int n);
}
