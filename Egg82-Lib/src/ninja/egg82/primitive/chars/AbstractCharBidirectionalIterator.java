package ninja.egg82.primitive.chars;

public abstract class AbstractCharBidirectionalIterator extends AbstractCharIterator implements CharBidirectionalIterator {
	//vars
	
	//constructor
	protected AbstractCharBidirectionalIterator() {
		super();
	}
	
	//public
	public char previousChar() {
		return previous().charValue();
	}
	public Character previous() {
		return Character.valueOf(previousChar());
	}
	
	public int back(final int n) {
		int i = n;
		
		while(i-- != 0 && hasPrevious()) {
			previousChar();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
