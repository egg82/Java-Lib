package ninja.egg82.primitive.chars;

import java.util.List;

public interface CharList extends List<Character>, Comparable<List<? extends Character>>, CharCollection {
	//functions
	CharListIterator iterator();
	CharListIterator charListIterator();
	CharListIterator charListIterator(int index);
	CharListIterator listIterator();
	CharListIterator listIterator(int index);
	
	CharList charSubList(int from, int to);
	CharList subList(int from, int to);
	
	void size(int size);
	
	void getElements(int from, char a[], int offset, int length);
	void removeElements(int from, int to);
	void addElements(int index, char a[]);
	void addElements(int index, char a[], int offset, int length);
	
	boolean add(char key);
	void add(int index, char key);
	
	boolean addAll(int index, CharCollection c);
	boolean addAll(int index, CharList c);
	boolean addAll(CharList c);
	
	char getChar(int index);
	int indexOf(char k);
	int lastIndexOf(char k);
	char removeChar(int index);
	char set(int index, char k);
}
