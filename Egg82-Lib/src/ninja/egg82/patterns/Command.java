package ninja.egg82.patterns;

import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.utils.ThreadUtil;

public abstract class Command {
	//vars
	private final EventHandler<CompleteEventArgs<?>> complete = new EventHandler<CompleteEventArgs<?>>();
	private final EventHandler<ExceptionEventArgs<?>> error = new EventHandler<ExceptionEventArgs<?>>();
	
	private long startTime = 0L;
	private long delay = 0L;
	
	//constructor
	public Command() {
		this(0);
	}
	public Command(int delay) {
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
		startTime = System.currentTimeMillis();
		if (delay == 0L) {
			ThreadUtil.submit(new Runnable() {
				public void run() {
					onExecute(System.currentTimeMillis() - startTime);
				}
			});
		} else {
			ThreadUtil.schedule(new Runnable() {
				public void run() {
					onExecute(System.currentTimeMillis() - startTime);
				}
			}, delay);
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