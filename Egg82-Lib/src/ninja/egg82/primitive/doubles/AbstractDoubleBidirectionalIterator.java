package ninja.egg82.primitive.doubles;

public abstract class AbstractDoubleBidirectionalIterator extends AbstractDoubleIterator implements DoubleBidirectionalIterator {
	//vars
	
	//constructor
	protected AbstractDoubleBidirectionalIterator() {
		super();
	}
	
	//public
	public double previousDouble() {
		return previous().doubleValue();
	}
	public Double previous() {
		return Double.valueOf(previousDouble());
	}
	
	public int back(final int n) {
		int i = n;
		
		while(i-- != 0 && hasPrevious()) {
			previousDouble();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
