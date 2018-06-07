package ninja.egg82.primitive.shorts;

import it.unimi.dsi.fastutil.Stack;

public interface ShortStack extends Stack<Short> {
	//functions
	void push(short k);
	short popShort();
	short topShort();
	short peekShort(int i);
}
