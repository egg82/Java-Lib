package ninja.egg82.patterns;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;

public abstract class Command {
	//vars
	private final EventHandler<EventArgs> complete = new EventHandler<EventArgs>();
	private final EventHandler<ExceptionEventArgs> error = new EventHandler<ExceptionEventArgs>();
	
	private Timer timer = null;
	private long startTime = 0L;
	
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
			timer = new Timer(delay, onTimer);
			timer.setRepeats(false);
		}
	}
	
	//public
	public final void start() {
		if (timer != null) {
			startTime = System.currentTimeMillis();
			timer.start();
		} else {
			new Thread(new Runnable() {
				public void run() {
					onExecute(0L);
				}
			}).start();
		}
	}
	
	public final EventHandler<EventArgs> onComplete() {
		return complete;
	}
	public final EventHandler<ExceptionEventArgs> onError() {
		return error;
	}
	
	//private
	protected abstract void onExecute(long elapsedMilliseconds);
	
	private ActionListener onTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			onExecute(System.currentTimeMillis() - startTime);
		}
	};
}