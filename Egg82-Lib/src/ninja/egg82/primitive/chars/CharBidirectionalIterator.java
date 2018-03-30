package ninja.egg82.primitive.chars;

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public interface CharBidirectionalIterator extends CharIterator, ObjectBidirectionalIterator<Character> {
	//functions
	char previousChar();
	int back(int n);
}
