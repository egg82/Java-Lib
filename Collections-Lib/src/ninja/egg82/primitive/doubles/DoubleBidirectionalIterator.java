package ninja.egg82.primitive.doubles;

import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;

public interface DoubleBidirectionalIterator extends DoubleIterator, ObjectBidirectionalIterator<Double> {
	//functions
	double previousDouble();
	int back(int n);
}
