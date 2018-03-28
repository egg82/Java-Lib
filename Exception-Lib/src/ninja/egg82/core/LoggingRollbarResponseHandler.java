package ninja.egg82.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import com.rollbar.sender.RollbarResponse;
import com.rollbar.sender.RollbarResponseCode;
import com.rollbar.sender.RollbarResponseHandler;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;
import ninja.egg82.patterns.tuples.pair.Int2Pair;

public class LoggingRollbarResponseHandler implements RollbarResponseHandler {
	//vars
	private IConcurrentDeque<Int2Pair<LogRecord>> logs = new DynamicConcurrentDeque<Int2Pair<LogRecord>>();
	private IConcurrentDeque<Int2Pair<Exception>> exceptions = new DynamicConcurrentDeque<Int2Pair<Exception>>();
	
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
					exceptions.add(new Int2Pair<Exception>(lastException, tries));
					lastException = null;
				} else if (lastLog != null) {
					logs.add(new Int2Pair<LogRecord>(lastLog, tries));
					lastLog = null;
				}
			} else {
				if (tries < 1) {
					if (lastException != null) {
						exceptions.add(new Int2Pair<Exception>(new Exception(lastException), tries + 1));
						lastException = null;
					} else if (lastLog != null) {
						logs.add(new Int2Pair<LogRecord>(lastLog, tries + 1));
						lastLog = null;
					}
				}
			}
		}
	}
	
	public List<Int2Pair<Exception>> getUnsentExceptions() {
		return new ArrayList<Int2Pair<Exception>>(exceptions);
	}
	public void setUnsentExceptions(List<Exception> list) {
		exceptions.clear();
		for (Exception ex : list) {
			exceptions.add(new Int2Pair<Exception>(ex, 0));
		}
	}
	public List<Int2Pair<LogRecord>> getUnsentLogs() {
		return new ArrayList<Int2Pair<LogRecord>>(logs);
	}
	public void setUnsentLogs(List<LogRecord> list) {
		logs.clear();
		for (LogRecord log : list) {
			logs.add(new Int2Pair<LogRecord>(log, 0));
		}
	}
	
	public void addLog(LogRecord log) {
		logs.add(new Int2Pair<LogRecord>(log, 0));
	}
	public void addException(Exception ex) {
		exceptions.add(new Int2Pair<Exception>(ex, 0));
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
