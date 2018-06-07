package ninja.egg82.primitive.shorts;

import java.util.List;

public interface ShortList extends List<Short>, Comparable<List<? extends Short>>, ShortCollection {
	//functions
	ShortListIterator iterator();
	ShortListIterator shortListIterator();
	ShortListIterator shortListIterator(int index);
	ShortListIterator listIterator();
	ShortListIterator listIterator(int index);
	
	ShortList shortSubList(int from, int to);
	ShortList subList(int from, int to);
	
	void size(int size);
	
	void getElements(int from, short a[], int offset, int length);
	void removeElements(int from, int to);
	void addElements(int index, short a[]);
	void addElements(int index, short a[], int offset, int length);
	
	boolean add(short key);
	void add(int index, short key);
	
	boolean addAll(int index, ShortCollection c);
	boolean addAll(int index, ShortList c);
	boolean addAll(ShortList c);
	
	short getShort(int index);
	int indexOf(short k);
	int lastIndexOf(short k);
	short removeShort(int index);
	short set(int index, short k);
}
