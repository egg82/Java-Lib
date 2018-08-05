package ninja.egg82.analytics.exceptions;

import java.util.Collection;
import java.util.logging.LogRecord;

import com.google.common.collect.ImmutableList;

import ninja.egg82.analytics.core.ExceptionLogContainer;

public interface IExceptionHandler {
	//functions
	void sendException(Throwable ex);
	void publish(LogRecord record);
	
	void sendWarning(String message);
	void sendInfo(String message);
	void sendDebug(String message);
	
	void close();
	
	boolean hasLimit();
	boolean isLimitReached();
	
	ImmutableList<ExceptionLogContainer> getUnsentLogs();
	void addLogs(Collection<ExceptionLogContainer> logs);
}
