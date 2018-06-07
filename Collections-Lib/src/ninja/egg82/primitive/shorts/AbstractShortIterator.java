package ninja.egg82.primitive.shorts;

public abstract class AbstractShortIterator implements ShortIterator {
	//vars
	
	//constructor
	protected AbstractShortIterator() {
		
	}
	
	//public
	public short nextShort() {
		return next().shortValue();
	}
	public Short next() {
		return Short.valueOf(nextShort());
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public int skip(final int n) {
		int i = n;
		
		while(i-- != 0 && hasNext()) {
			nextShort();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
