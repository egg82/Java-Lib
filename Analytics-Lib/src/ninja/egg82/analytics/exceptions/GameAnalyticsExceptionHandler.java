package ninja.egg82.analytics.exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.json.simple.JSONObject;

import com.google.common.collect.ImmutableList;

import ninja.egg82.analytics.GameAnalyticsHandler;
import ninja.egg82.analytics.core.ExceptionLogContainer;
import ninja.egg82.analytics.core.LogLevel;

public class GameAnalyticsExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private GameAnalyticsHandler handler = null;
	
	//constructor
	public GameAnalyticsExceptionHandler(String gameKey, String secretKey, String version, String userId, String threadName) {
		if (gameKey == null) {
			throw new IllegalArgumentException("gameKey cannot be null.");
		}
		if (secretKey == null) {
			throw new IllegalArgumentException("secretKey cannot be null.");
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
		
		threadName += "-Exception";
		
		handler = new GameAnalyticsHandler(gameKey, secretKey, version, userId, 1, threadName);
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
		return false;
	}
	public boolean isLimitReached() {
		return false;
	}
	
	public ImmutableList<ExceptionLogContainer> getUnsentLogs() {
		ImmutableList<JSONObject> events = handler.getUnsentEvents();
		
		List<ExceptionLogContainer> logs = new ArrayList<ExceptionLogContainer>();
		for (JSONObject event : events) {
			if (!event.containsKey("category")) {
				continue;
			}
			if (!((String) event.get("category")).equalsIgnoreCase("error")) {
				continue;
			}
			if (!event.containsKey("message") || !event.containsKey("severity")) {
				continue;
			}
			
			LogLevel level = null;
			
			try {
				LogLevel.valueOf(((String) event.get("severity")).toUpperCase());
			} catch (Exception ex) {
				level = LogLevel.ERROR;
			}
			
			logs.add(new ExceptionLogContainer((String) event.get("message"), level));
		}
		
		return ImmutableList.copyOf(logs);
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
