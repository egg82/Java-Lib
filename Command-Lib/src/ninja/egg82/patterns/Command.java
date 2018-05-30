package ninja.egg82.patterns;

import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public abstract class Command implements ICommand {
	//vars
	private final EventHandler<CompleteEventArgs<?>> complete = new EventHandler<CompleteEventArgs<?>>();
	private final EventHandler<ExceptionEventArgs<?>> error = new EventHandler<ExceptionEventArgs<?>>();
	
	private long startTime = 0L;
	private long delay = 0;
	
	//constructor
	public Command() {
		this(0);
	}
	public Command(long delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("delay cannot be less than zero.");
		} else if (delay == 0) {
			return;
		} else {
			this.delay = delay;
		}
	}
	
	//public
	public final void start() {
		if (delay == 0L) {
			onExecute(0L);
		} else {
			startTime = System.currentTimeMillis();
			try {
				Thread.sleep(delay);
			} catch (Exception ex) {
				
			}
			onExecute(System.currentTimeMillis() - startTime);
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
