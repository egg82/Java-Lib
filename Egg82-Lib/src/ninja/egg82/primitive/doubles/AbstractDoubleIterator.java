package ninja.egg82.primitive.doubles;

public abstract class AbstractDoubleIterator implements DoubleIterator {
	//vars
	
	//constructor
	protected AbstractDoubleIterator() {
		
	}
	
	//public
	public double nextDouble() {
		return next().doubleValue();
	}
	public Double next() {
		return Double.valueOf(nextDouble());
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public int skip(final int n) {
		int i = n;
		
		while(i-- != 0 && hasNext()) {
			nextDouble();
		}
		
		return n - i - 1;
	}
	
	//private
	
}
