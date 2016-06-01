package com.egg82.patterns.command;

import com.egg82.events.patterns.command.CommandEvent;
import com.egg82.patterns.Observer;
import com.egg82.patterns.TriFunction;

public class NestedCommand extends Command {
	//vars
	private Command command = null;
	private Observer commandObserver = new Observer();
	
	//constructor
	public NestedCommand(Command command) {
		this(command, 0);
	}
	public NestedCommand(Command command, int delay) {
		super(delay);
		this.command = command;
		
		commandObserver.add(onCommandObserverNotify);
	}
	
	//public
	
	//private
	protected void execute() {
		if (command == null) {
			return;
		}
		
		Observer.add(Command.OBSERVERS, commandObserver);
		command.start();
	}
	protected void postExecute(Object data) {
		dispatch(CommandEvent.COMPLETE, null);
	}
	private TriFunction<Object, String, Object, Void> onCommandObserverNotify = (sender, event, args) -> {
		if (sender != command) {
			return null;
		}
		if (event == CommandEvent.COMPLETE) {
			Observer.remove(Command.OBSERVERS, commandObserver);
			postExecute(data);
		} else if (event == CommandEvent.ERROR) {
			Observer.remove(Command.OBSERVERS, commandObserver);
			dispatch(CommandEvent.ERROR, data);
			postExecute(data);
		}
		return null;
	};
}