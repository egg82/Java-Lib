package ninja.egg82.analytics.exceptions;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.google.common.collect.ImmutableList;

import ninja.egg82.analytics.core.ExceptionLogContainer;
import ninja.egg82.analytics.core.LogLevel;
import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;

public class NullExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private IConcurrentDeque<ExceptionLogContainer> queuedLogs = new DynamicConcurrentDeque<ExceptionLogContainer>();
	
	//constructor
	public NullExceptionHandler() {
		
	}
	
	//public
	public void sendException(Throwable ex) {
		if (ex == null) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(ex));
	}
	public void publish(LogRecord record) {
		if (record == null) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(record));
	}
	
	public void sendWarning(String message) {
		if (message == null) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(message, LogLevel.WARNING));
	}
	public void sendInfo(String message) {
		if (message == null) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(message, LogLevel.INFO));
	}
	public void sendDebug(String message) {
		if (message == null) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(message, LogLevel.DEBUG));
	}
	
	public void flush() {
		
	}
	public void close() {
		
	}
	
	public boolean hasLimit() {
		return false;
	}
	public boolean isLimitReached() {
		return false;
	}
	
	public ImmutableList<ExceptionLogContainer> getUnsentLogs() {
		return ImmutableList.copyOf(queuedLogs);
	}
	public void addLogs(Collection<ExceptionLogContainer> logs) {
		if (logs == null) {
			return;
		}
		
		for (ExceptionLogContainer log : logs) {
			if (log == null) {
				continue;
			}
			
			queuedLogs.add(log);
		}
	}
	
	//private
	
}
