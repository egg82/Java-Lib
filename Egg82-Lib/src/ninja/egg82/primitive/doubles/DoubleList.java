package ninja.egg82.primitive.doubles;

import java.util.List;

public interface DoubleList extends List<Double>, Comparable<List<? extends Double>>, DoubleCollection {
	//functions
	DoubleListIterator iterator();
	DoubleListIterator doubleListIterator();
	DoubleListIterator doubleListIterator(int index);
	DoubleListIterator listIterator();
	DoubleListIterator listIterator(int index);
	
	DoubleList doubleSubList(int from, int to);
	DoubleList subList(int from, int to);
	
	void size(int size);
	
	void getElements(int from, double a[], int offset, int length);
	void removeElements(int from, int to);
	void addElements(int index, double a[]);
	void addElements(int index, double a[], int offset, int length);
	
	boolean add(double key);
	void add(int index, double key);
	
	boolean addAll(int index, DoubleCollection c);
	boolean addAll(int index, DoubleList c);
	boolean addAll(DoubleList c);
	
	double getDouble(int index);
	int indexOf(double k);
	int lastIndexOf(double k);
	double removeDouble(int index);
	double set(int index, double k);
}
