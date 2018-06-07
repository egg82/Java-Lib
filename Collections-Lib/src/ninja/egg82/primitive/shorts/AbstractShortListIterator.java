package ninja.egg82.primitive.shorts;

public abstract class AbstractShortListIterator extends AbstractShortBidirectionalIterator implements ShortListIterator {
	//vars
	
	//constructor
	protected AbstractShortListIterator() {
		super();
	}
	
	//public
	public void set(Short ok) {
		set(ok.shortValue());
	}
	public void add(Short ok) {
		add(ok.shortValue());
	}
	public void set(short k) {
		throw new UnsupportedOperationException();
	}
	public void add(short k) {
		throw new UnsupportedOperationException();
	}
	
	//private
	
}
