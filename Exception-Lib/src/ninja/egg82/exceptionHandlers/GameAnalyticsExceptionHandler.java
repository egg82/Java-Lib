package ninja.egg82.exceptionHandlers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Timer;

import ninja.egg82.core.GameAnalyticsAPI;
import ninja.egg82.exceptionHandlers.builders.IBuilder;

public class GameAnalyticsExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private GameAnalyticsAPI api = null;
	
	private ArrayList<LogRecord> logs = new ArrayList<LogRecord>();
	private ArrayList<Exception> exceptions = new ArrayList<Exception>();
	
	private Timer cleanupTimer = null;
	private ArrayList<Thread> errorThreads = new ArrayList<Thread>();
	
	//constructor
	public GameAnalyticsExceptionHandler() {
		Logger.getLogger("ninja.egg82.core.PasswordHasher").addHandler(this);
		Logger.getLogger("ninja.egg82.patterns.events.EventHandler").addHandler(this);
	}
	
	//public
	public void connect(IBuilder builder) {
		String[] params = builder.getParams();
		if (params == null || params.length != 4) {
			throw new IllegalArgumentException("params must have a length of 4. Use ninja.egg82.exceptionHandlers.builders.GameAnalyticsBuilder");
		}
		
		api = new GameAnalyticsAPI(params[0], params[1], params[2], params[3]);
		api.handleUncaughtErrors();
		
		for (LogRecord record : logs) {
			if (record.getThrown() != null) {
				api.log(record.getThrown(), record.getLevel());
			} else if (record.getMessage() != null) {
				api.log(record.getMessage(), record.getLevel());
			}
		}
		logs.clear();
		for (Exception ex : exceptions) {
			api.log(ex);
		}
		exceptions.clear();
		
		cleanupTimer = new Timer(5 * 60 * 1000, onCleanupTimer);
		cleanupTimer.setRepeats(true);
		cleanupTimer.start();
	}
	public void disconnect() {
		if (api != null) {
			for (Thread t : errorThreads) {
				api.unhandleUncaughtErrors(t);
			}
			errorThreads.clear();
		}
	}
	
	public void addThread(Thread thread) {
		if (api != null && !errorThreads.contains(thread)) {
			errorThreads.add(thread);
			api.handleUncaughtErrors(thread);
		}
	}
	public void removeThread(Thread thread) {
		if (api != null && errorThreads.contains(thread)) {
			api.unhandleUncaughtErrors(thread);
			errorThreads.remove(thread);
		}
	}
	public void silentException(Exception ex) {
		if (api != null) {
			api.log(ex);
		} else {
			exceptions.add(ex);
		}
	}
	public void throwException(RuntimeException ex) {
		if (api != null) {
			api.log(ex);
		} else {
			exceptions.add(ex);
		}
		throw ex;
	}
	
	public void publish(LogRecord record) {
		if (api != null) {
			if (record.getThrown() != null) {
				api.log(record.getThrown(), record.getLevel());
			} else if (record.getMessage() != null) {
				api.log(record.getMessage(), record.getLevel());
			}
		} else {
			logs.add(record);
		}
	}
	public void flush() {
		
	}
	public void close() throws SecurityException {
		
	}
	
	public List<Exception> getUnsentExceptions() {
		return exceptions;
	}
	public void setUnsentExceptions(List<Exception> list) {
		exceptions.clear();
		exceptions.addAll(list);
		
		if (api != null) {
			for (Exception ex : exceptions) {
				api.log(ex);
			}
			exceptions.clear();
		}
	}
	public List<LogRecord> getUnsentLogs() {
		return logs;
	}
	public void setUnsentLogs(List<LogRecord> list) {
		logs.clear();
		logs.addAll(list);
		
		if (api != null) {
			for (LogRecord record : logs) {
				if (record.getThrown() != null) {
					api.log(record.getThrown(), record.getLevel());
				} else if (record.getMessage() != null) {
					api.log(record.getMessage(), record.getLevel());
				}
			}
			logs.clear();
		}
	}
	
	public boolean isLimitReached() {
		return false;
	}
	
	//private
	private ActionListener onCleanupTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			errorThreads.removeIf((v) -> (!v.isAlive()));
		}
	};
}
