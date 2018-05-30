package ninja.egg82.primitive.doubles;

import it.unimi.dsi.fastutil.Stack;

public interface DoubleStack extends Stack<Double> {
	//functions
	void push(double k);
	double popDouble();
	double topDouble();
	double peekDouble(int i);
}
