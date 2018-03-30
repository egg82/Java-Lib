package ninja.egg82.primitive.chars;

public abstract class AbstractCharIterator implements CharIterator {
	//vars
	
	//constructor
	protected AbstractCharIterator() {
		
	}
	
	//public
	public char nextChar() {
		return next().charValue();
	}
	public Character next() {
		return Character.valueOf(nextChar());
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public int skip(final int n) {
		int i = n;
		
		while(i-- != 0 && hasNext()) {
			nextChar();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
