package ninja.egg82.primitive.chars;

import java.util.Collection;

public interface CharCollection extends Collection<Character>, CharIterable {
	//functions
	CharIterator iterator();
	CharIterator charIterator();
	
	<T> T[] toArray(T[] a);
	
	boolean contains(char key);
	
	char[] toCharArray();
	char[] toCharArray(char a[]);
	char[] toArray(char a[]);
	
	boolean add(char key);
	boolean rem(char key);
	
	boolean addAll(CharCollection c);
	boolean containsAll(CharCollection c);
	boolean removeAll(CharCollection c);
	boolean retainAll(CharCollection c);
}
