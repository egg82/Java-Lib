package ninja.egg82.primitive.doubles;

public abstract class AbstractDoubleListIterator extends AbstractDoubleBidirectionalIterator implements DoubleListIterator {
	//vars
	
	//constructor
	protected AbstractDoubleListIterator() {
		super();
	}
	
	//public
	public void set(Double ok) {
		set(ok.doubleValue());
	}
	public void add(Double ok) {
		add(ok.doubleValue());
	}
	public void set(double k) {
		throw new UnsupportedOperationException();
	}
	public void add(double k) {
		throw new UnsupportedOperationException();
	}
	
	//private
	
}
