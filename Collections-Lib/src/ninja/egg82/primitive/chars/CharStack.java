package ninja.egg82.primitive.chars;

import it.unimi.dsi.fastutil.Stack;

public interface CharStack extends Stack<Character> {
	//functions
	void push(char k);
	char popChar();
	char topChar();
	char peekChar(int i);
}
