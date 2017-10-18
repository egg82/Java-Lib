package ninja.egg82.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import com.rollbar.sender.RollbarResponse;
import com.rollbar.sender.RollbarResponseCode;
import com.rollbar.sender.RollbarResponseHandler;

import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;
import ninja.egg82.patterns.tuples.Pair;

public class LoggingRollbarResponseHandler implements RollbarResponseHandler {
	//vars
	private IObjectPool<Pair<LogRecord, Integer>> logs = new DynamicObjectPool<Pair<LogRecord, Integer>>();
	private IObjectPool<Pair<Exception, Integer>> exceptions = new DynamicObjectPool<Pair<Exception, Integer>>();
	
	private volatile Exception lastException = null;
	private volatile int tries = 0;
	private volatile LogRecord lastLog = null;
	
	public volatile boolean limitReached = false;
	
	//constructor
	public LoggingRollbarResponseHandler() {
		
	}
	
	//public
	public void handleResponse(RollbarResponse response) {
		if (!response.isSuccessful()) {
			if (response.statusCode() == RollbarResponseCode.TooManyRequests) {
				limitReached = true;
				
				if (lastException != null) {
					exceptions.add(new Pair<Exception, Integer>(lastException, tries));
					lastException = null;
				} else if (lastLog != null) {
					logs.add(new Pair<LogRecord, Integer>(lastLog, tries));
					lastLog = null;
				}
			} else {
				if (tries < 1) {
					if (lastException != null) {
						exceptions.add(new Pair<Exception, Integer>(new Exception(lastException), tries + 1));
						lastException = null;
					} else if (lastLog != null) {
						logs.add(new Pair<LogRecord, Integer>(lastLog, tries + 1));
						lastLog = null;
					}
				}
			}
		}
	}
	
	public List<Pair<Exception, Integer>> getUnsentExceptions() {
		return new ArrayList<Pair<Exception, Integer>>(exceptions);
	}
	public void setUnsentExceptions(List<Exception> list) {
		exceptions.clear();
		list.forEach((v) -> {
			exceptions.add(new Pair<Exception, Integer>(v, 0));
		});
	}
	public List<Pair<LogRecord, Integer>> getUnsentLogs() {
		return new ArrayList<Pair<LogRecord, Integer>>(logs);
	}
	public void setUnsentLogs(List<LogRecord> list) {
		logs.clear();
		list.forEach((v) -> {
			logs.add(new Pair<LogRecord, Integer>(v, 0));
		});
	}
	
	public void addLog(LogRecord log) {
		logs.add(new Pair<LogRecord, Integer>(log, 0));
	}
	public void addException(Exception ex) {
		exceptions.add(new Pair<Exception, Integer>(ex, 0));
	}
	
	public void setLastException(Exception ex) {
		lastException = ex;
		lastLog = null;
	}
	public void setTries(int tries) {
		this.tries = tries;
	}
	public void setLastLog(LogRecord record) {
		lastException = null;
		lastLog = record;
	}
	
	public void clearLogs() {
		logs.clear();
	}
	public void clearExceptions() {
		exceptions.clear();
	}
	
	//private
	
}
