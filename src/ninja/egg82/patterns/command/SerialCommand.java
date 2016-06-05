package ninja.egg82.patterns.command;

import org.apache.commons.lang3.ArrayUtils;

import ninja.egg82.events.patterns.command.CommandEvent;
import ninja.egg82.patterns.Observer;
import ninja.egg82.patterns.TriFunction;

public class SerialCommand extends Command {
	//vars
	private Object startData = null;
	private Command[] commands = null;
	private int completed = 0;
	
	private Observer commandObserver = new Observer();
	
	//constructor
	public SerialCommand() {
		this(0, null, null);
	}
	public SerialCommand(int delay) {
		this(delay, null, null);
	}
	public SerialCommand(int delay, Command[] serializableCommands) {
		this(delay, serializableCommands, null);
	}
	public SerialCommand(int delay, Command[] serializableCommands, Object startData) {
		super(delay);
		
		this.startData = startData;
		this.commands = serializableCommands;
		
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
		
		commands[0].startSerialized(startData);
	}
	private TriFunction<Object, String, Object, Void> onCommandObserverNotify = (sender, event, args) -> {
		if (!ArrayUtils.contains(commands, sender)) {
			return null;
		}
		
		if (event == CommandEvent.COMPLETE) {
			handleData(data);
		} else if (event == CommandEvent.ERROR) {
			Observer.remove(Command.OBSERVERS, commandObserver);
			dispatch(CommandEvent.ERROR, data);
		}
		
		return null;
	};
	private void handleData(Object data) {
		completed++;
		
		if (completed == commands.length) {
			Observer.remove(Command.OBSERVERS, commandObserver);
			dispatch(CommandEvent.COMPLETE, data);
			return;
		}
		
		commands[completed].startSerialized(data);
	}
}