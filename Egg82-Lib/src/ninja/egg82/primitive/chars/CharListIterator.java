package ninja.egg82.primitive.chars;

import java.util.ListIterator;

public interface CharListIterator extends ListIterator<Character>, CharBidirectionalIterator {
	//functions
	void set(char k);
	void add(char k);
}
