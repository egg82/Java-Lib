package ninja.egg82.analytics.exceptions;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.google.common.collect.ImmutableList;
import com.rollbar.Rollbar;
import com.rollbar.payload.data.Person;

import ninja.egg82.analytics.core.ExceptionLogContainer;
import ninja.egg82.analytics.core.LogLevel;
import ninja.egg82.analytics.core.RetryRollbarResponseHandler;

public class RollbarExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private Rollbar rollbar = null;
	
	private RetryRollbarResponseHandler handler = null;
	
	//constructor
	public RollbarExceptionHandler(String accessToken, String environment, String version, String userId, String threadName) {
		if (accessToken == null) {
			throw new IllegalArgumentException("accessToken cannot be null.");
		}
		if (environment == null) {
			throw new IllegalArgumentException("environment cannot be null.");
		}
		if (version == null) {
			throw new IllegalArgumentException("version cannot be null.");
		}
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null.");
		}
		if (threadName == null) {
			throw new IllegalArgumentException("threadName cannot be null.");
		}
		
		handler = new RetryRollbarResponseHandler(threadName);
		
		rollbar = new Rollbar(accessToken, environment).codeVersion(version).responseHandler(handler).person(new Person(userId));
		handler.setRollbar(rollbar);
	}
	
	//public
	public void sendException(Throwable ex) {
		if (ex == null) {
			return;
		}
		
		handler.sendException(ex);
	}
	public void publish(LogRecord record) {
		if (record == null) {
			return;
		}
		
		handler.sendRecord(record);
	}
	
	public void sendWarning(String message) {
		if (message == null) {
			return;
		}
		
		handler.sendMessage(message, LogLevel.WARNING);
	}
	public void sendInfo(String message) {
		if (message == null) {
			return;
		}
		
		handler.sendMessage(message, LogLevel.INFO);
	}
	public void sendDebug(String message) {
		if (message == null) {
			return;
		}
		
		handler.sendMessage(message, LogLevel.DEBUG);
	}
	
	public void flush() {
		
	}
	public void close() {
		
	}
	
	public boolean hasLimit() {
		return true;
	}
	public boolean isLimitReached() {
		return handler.isLimitReached();
	}
	
	public ImmutableList<ExceptionLogContainer> getUnsentLogs() {
		return ImmutableList.copyOf(handler.getUnsentLogs());
	}
	public void addLogs(Collection<ExceptionLogContainer> logs) {
		if (logs == null) {
			return;
		}
		
		for (ExceptionLogContainer log : logs) {
			if (log == null) {
				continue;
			}
			
			if (log.getEx() != null) {
				handler.sendException(log.getEx());
			} else if (log.getRecord() != null) {
				handler.sendRecord(log.getRecord());
			} else if (log.getMessage() != null) {
				handler.sendMessage(log.getMessage(), log.getLevel());
			}
		}
	}
	
	//private
	
}
