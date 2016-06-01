package com.egg82.patterns.command;

import org.apache.commons.lang3.ArrayUtils;

import com.egg82.events.patterns.command.CommandEvent;
import com.egg82.patterns.Observer;
import com.egg82.patterns.TriFunction;

public class ParallelCommand extends Command {
	//vars
	private Command[] commands = null;
	private int completed = 0;
	
	private Observer commandObserver = new Observer();
	
	//constructor
	public ParallelCommand() {
		this(0, null);
	}
	public ParallelCommand(int delay) {
		this(delay, null);
	}
	public ParallelCommand(int delay, Command[] commands) {
		super(delay);
		
		this.commands = commands;
		commandObserver.add(onCommandObserverNotify);
	}
	
	//public
	
	//private
	protected void execute() {
		if (commands == null || commands.length == 0) {
			dispatch(CommandEvent.COMPLETE, null);
			return;
		}
		
		Observer.add(Command.OBSERVERS, commandObserver);
		completed = 0;
		
		for (int i = 0; i < commands.length; i++) {
			commands[i].start();
		}
	}
	
	private TriFunction<Object, String, Object, Void> onCommandObserverNotify = (sender, event, args) -> {
		if (!ArrayUtils.contains(commands, sender)) {
			return null;
		}
		
		completed++;
		if (event == CommandEvent.ERROR) {
			dispatch(CommandEvent.ERROR, data);
		}
		if (completed == commands.length) {
			Observer.remove(Command.OBSERVERS, commandObserver);
			dispatch(CommandEvent.COMPLETE, null);
		}
		
		return null;
	};
}