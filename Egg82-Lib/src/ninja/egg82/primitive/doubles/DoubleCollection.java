package ninja.egg82.primitive.doubles;

import java.util.Collection;

public interface DoubleCollection extends Collection<Double>, DoubleIterable {
	//functions
	DoubleIterator iterator();
	DoubleIterator doubleIterator();
	
	<T> T[] toArray(T[] a);
	
	boolean contains(double key);
	
	double[] toDoubleArray();
	double[] toDoubleArray(double a[]);
	double[] toArray(double a[]);
	
	boolean add(double key);
	boolean rem(double key);
	
	boolean addAll(DoubleCollection c);
	boolean containsAll(DoubleCollection c);
	boolean removeAll(DoubleCollection c);
	boolean retainAll(DoubleCollection c);
}
