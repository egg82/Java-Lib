package ninja.egg82.patterns;

import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public abstract class SynchronousCommand {
	//vars
	private final EventHandler<CompleteEventArgs<?>> complete = new EventHandler<CompleteEventArgs<?>>();
	private final EventHandler<ExceptionEventArgs<?>> error = new EventHandler<ExceptionEventArgs<?>>();
	
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
		if (timer != 0) {
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
	
	public final EventHandler<CompleteEventArgs<?>> onComplete() {
		return complete;
	}
	public final EventHandler<ExceptionEventArgs<?>> onError() {
		return error;
	}
	
	//private
	protected abstract void onExecute(long elapsedMilliseconds);
}
