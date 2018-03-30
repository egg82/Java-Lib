package ninja.egg82.primitive.doubles;

import java.util.Iterator;

public interface DoubleIterator extends Iterator<Double> {
	//functions
	double nextDouble();
	int skip(int n);
}
