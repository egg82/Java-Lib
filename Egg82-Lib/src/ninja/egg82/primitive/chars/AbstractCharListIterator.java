package ninja.egg82.primitive.chars;

public abstract class AbstractCharListIterator extends AbstractCharBidirectionalIterator implements CharListIterator {
	//vars
	
	//constructor
	protected AbstractCharListIterator() {
		super();
	}
	
	//public
	public void set(Character ok) {
		set(ok.charValue());
	}
	public void add(Character ok) {
		add(ok.charValue());
	}
	public void set(char k) {
		throw new UnsupportedOperationException();
	}
	public void add(char k) {
		throw new UnsupportedOperationException();
	}
	
	//private
	
}
