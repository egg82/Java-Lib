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
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;

public class GameAnalyticsExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private GameAnalyticsAPI api = null;
	
	private IObjectPool<LogRecord> logs = new DynamicObjectPool<LogRecord>();
	private IObjectPool<Exception> exceptions = new DynamicObjectPool<Exception>();
	
	private Timer cleanupTimer = null;
	private IObjectPool<Thread> errorThreads = new DynamicObjectPool<Thread>();
	
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
		if (api != null && !errorThreads.add(thread)) {
			api.handleUncaughtErrors(thread);
		}
	}
	public void removeThread(Thread thread) {
		if (api != null && errorThreads.remove(thread)) {
			api.unhandleUncaughtErrors(thread);
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
		return new ArrayList<Exception>(exceptions);
	}
	public void setUnsentExceptions(List<Exception> list) {
		if (api != null) {
			for (Exception ex : list) {
				api.log(ex);
			}
		} else {
			exceptions.clear();
			exceptions.addAll(list);
		}
}
	public List<LogRecord> getUnsentLogs() {
		return new ArrayList<LogRecord>(logs);
	}
	public void setUnsentLogs(List<LogRecord> list) {
		if (api != null) {
			for (LogRecord record : list) {
				if (record.getThrown() != null) {
					api.log(record.getThrown(), record.getLevel());
				} else if (record.getMessage() != null) {
					api.log(record.getMessage(), record.getLevel());
				}
			}
		} else {
			logs.clear();
			logs.addAll(list);
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
