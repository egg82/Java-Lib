package ninja.egg82.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import com.rollbar.sender.RollbarResponse;
import com.rollbar.sender.RollbarResponseCode;
import com.rollbar.sender.RollbarResponseHandler;

import ninja.egg82.patterns.tuples.Pair;

public class LoggingRollbarResponseHandler implements RollbarResponseHandler {
	//vars
	private ArrayList<Pair<LogRecord, Integer>> logs = new ArrayList<Pair<LogRecord, Integer>>();
	private ArrayList<Pair<Exception, Integer>> exceptions = new ArrayList<Pair<Exception, Integer>>();
	
	private Exception lastException = null;
	private int tries = 0;
	private LogRecord lastLog = null;
	
	public boolean limitReached = false;
	
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
				}
			} else {
				if (tries < 1) {
					if (lastException != null) {
						exceptions.add(new Pair<Exception, Integer>(new Exception(lastException), tries + 1));
						lastException = null;
					} else if (lastLog != null) {
						logs.add(new Pair<LogRecord, Integer>(lastLog, tries + 1));
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Pair<Exception, Integer>> getUnsentExceptions() {
		return (List<Pair<Exception, Integer>>) exceptions.clone();
	}
	public void setUnsentExceptions(List<Exception> list) {
		exceptions.clear();
		list.forEach((v) -> {
			exceptions.add(new Pair<Exception, Integer>(v, 0));
		});
	}
	@SuppressWarnings("unchecked")
	public List<Pair<LogRecord, Integer>> getUnsentLogs() {
		return (List<Pair<LogRecord, Integer>>) logs.clone();
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
