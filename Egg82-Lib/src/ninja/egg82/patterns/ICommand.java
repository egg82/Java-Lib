package ninja.egg82.patterns;

import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.ExceptionEventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface ICommand {
	//functions
	void start();
	
	EventHandler<CompleteEventArgs<?>> onComplete();
	EventHandler<ExceptionEventArgs<?>> onError();
}
