package ninja.egg82.primitive.shorts;

public abstract class AbstractShortBidirectionalIterator extends AbstractShortIterator implements ShortBidirectionalIterator {
	//vars
	
	//constructor
	protected AbstractShortBidirectionalIterator() {
		super();
	}
	
	//public
	public short previousShort() {
		return previous().shortValue();
	}
	public Short previous() {
		return Short.valueOf(previousShort());
	}
	
	public int back(final int n) {
		int i = n;
		
		while(i-- != 0 && hasPrevious()) {
			previousShort();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
