package ninja.egg82.patterns;

import java.util.ArrayList;

public abstract class SynchronousCommand {
	//vars
	public static final ArrayList<Observer> OBSERVERS = new ArrayList<Observer>();
	
	private long startTime = 0L;
	private int timer = 0;
	
	//constructor
	public SynchronousCommand() {
		this(0);
	}
	public SynchronousCommand(int delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("delay cannot be less than zero.");
		} else if (delay == 0) {
			return;
		} else {
			timer = delay;
		}
	}
	
	//public
	public final void start() {
		if (timer == 0) {
			startTime = System.currentTimeMillis();
			try {
				Thread.sleep(timer);
			} catch (Exception ex) {
				
			}
			onExecute(System.currentTimeMillis() - startTime);
		} else {
			onExecute(0L);
		}
	}
	
	//private
	protected abstract void onExecute(long elapsedMilliseconds);
	
	protected final void dispatch(String event, Object data) {
		Observer.dispatch(OBSERVERS, this, event, data);
	}
}
