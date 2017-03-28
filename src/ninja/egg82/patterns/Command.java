package ninja.egg82.patterns;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

public abstract class Command {
	//vars
	public static final ArrayList<Observer> OBSERVERS = new ArrayList<Observer>();
	
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
	
	//private
	protected abstract void onExecute(long elapsedMilliseconds);
	
	private ActionListener onTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			onExecute(System.currentTimeMillis() - startTime);
		}
	};
	protected final void dispatch(String event, Object data) {
		Observer.dispatch(OBSERVERS, this, event, data);
	}
}