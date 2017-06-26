package ninja.egg82.patterns;

public interface IEventDispatcher {
	//functions
	void dispatch(String event, Object data);
}
