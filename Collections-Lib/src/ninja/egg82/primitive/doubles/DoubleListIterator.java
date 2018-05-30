package ninja.egg82.primitive.doubles;

import java.util.ListIterator;

public interface DoubleListIterator extends ListIterator<Double>, DoubleBidirectionalIterator {
	//functions
	void set(double k);
	void add(double k);
}
