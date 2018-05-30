package ninja.egg82.primitive.chars;

import java.util.Iterator;

public interface CharIterator extends Iterator<Character> {
	//functions
	char nextChar();
	int skip(int n);
}
