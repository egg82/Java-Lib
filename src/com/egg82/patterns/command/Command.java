package com.egg82.patterns.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import com.egg82.events.patterns.command.CommandEvent;
import com.egg82.patterns.Observer;

public class Command {
	//vars
	public static final ArrayList<Observer> OBSERVERS = new ArrayList<Observer>();
	
	private Timer timer = null;
	protected Object data = null;
	
	//constructor
	public Command() {
		this(0);
	}
	public Command(int delay) {
		if (delay <= 0) {
			return;
		}
		
		timer = new Timer(delay, onTimer);
		timer.setRepeats(false);
	}
	
	//public
	public void start() {
		if (timer != null) {
			timer.start();
			return;
		}
		execute();
	}
	public void startSerialized(Object data) {
		this.data = data;
		if (timer != null) {
			timer.start();
			return;
		}
		execute();
	}
	
	//private
	protected void execute() {
		dispatch(CommandEvent.COMPLETE, null);
	}
	private ActionListener onTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			dispatch(CommandEvent.TIMER, null);
			execute();
		}
	};
	protected void dispatch(String event, Object data) {
		Observer.dispatch(OBSERVERS, this, event, data);
	}
}