package ninja.egg82.exceptionHandlers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Timer;

import com.rollbar.Rollbar;
import com.rollbar.payload.data.Person;

import ninja.egg82.core.LoggingRollbarResponseHandler;
import ninja.egg82.exceptionHandlers.builders.IBuilder;
import ninja.egg82.exceptions.ArgumentNullException;

public class RollbarExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private Rollbar rollbar = null;
	private LoggingRollbarResponseHandler responseHandler = new LoggingRollbarResponseHandler();
	
	private Timer resendTimer = null;
	private Timer cleanupTimer = null;
	private ArrayList<Thread> errorThreads = new ArrayList<Thread>();
	
	//constructor
	public RollbarExceptionHandler() {
		Logger.getLogger("ninja.egg82.core.PasswordHasher").addHandler(this);
		Logger.getLogger("ninja.egg82.patterns.events.EventHandler").addHandler(this);
	}
	
	//public
	public void connect(IBuilder builder) {
		String[] params = builder.getParams();
		if (params == null || params.length != 4) {
			throw new IllegalArgumentException("params must have a length of 4. Use ninja.egg82.exceptionHandlers.builders.RollbarBuilder");
		}
		rollbar = new Rollbar(params[0], params[1]).codeVersion(params[2]).responseHandler(responseHandler).person(new Person(params[3]));
		handleUncaughtErrors(Thread.currentThread());
		
		List<LogRecord> records = responseHandler.getUnsentLogs();
		responseHandler.clearLogs();
		for (LogRecord record : records) {
			rewriteFilename(record);
			responseHandler.setLastLog(record);
			if (record.getThrown() != null) {
				try {
					rollbar.log(record.getThrown(), getLevel(record.getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			} else if (record.getMessage() != null) {
				try {
					rollbar.log(record.getMessage(), getLevel(record.getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			}
		}
		List<Exception> exceptions = responseHandler.getUnsentExceptions();
		responseHandler.clearExceptions();
		for (Exception ex : exceptions) {
			rewriteFilename(ex);
			responseHandler.setLastException(ex);
			try {
				rollbar.log(ex);
			} catch (Exception ex2) {
				responseHandler.addException(ex2);
			}
		}
		
		resendTimer = new Timer(60 * 60 * 1000, onResendTimer);
		resendTimer.setRepeats(true);
		resendTimer.start();
		
		cleanupTimer = new Timer(5 * 60 * 1000, onCleanupTimer);
		cleanupTimer.setRepeats(true);
		cleanupTimer.start();
	}
	public void disconnect() {
		for (Thread t : errorThreads) {
			unhandleUncaughtErrors(t);
		}
		errorThreads.clear();
	}
	
	public void addThread(Thread thread) {
		if (rollbar != null) {
			handleUncaughtErrors(thread);
			errorThreads.add(thread);
		}
	}
	public void silentException(Exception ex) {
		if (rollbar != null) {
			rewriteFilename(ex);
			responseHandler.setLastException(ex);
			try {
				rollbar.log(ex);
			} catch (Exception ex2) {
				responseHandler.addException(ex2);
			}
		} else {
			responseHandler.addException(ex);
		}
	}
	public void throwException(RuntimeException ex) {
		if (rollbar != null) {
			rewriteFilename(ex);
			responseHandler.setLastException(ex);
			try {
				rollbar.log(ex);
			} catch (Exception ex2) {
				responseHandler.addException(ex2);
			}
		} else {
			responseHandler.addException(ex);
		}
		throw ex;
	}
	
	public void publish(LogRecord record) {
		if (rollbar != null) {
			rewriteFilename(record);
			responseHandler.setLastLog(record);
			if (record.getThrown() != null) {
				try {
					rollbar.log(record.getThrown(), getLevel(record.getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			} else if (record.getMessage() != null) {
				try {
					rollbar.log(record.getMessage(), getLevel(record.getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			}
		} else {
			responseHandler.addLog(record);
		}
	}
	public void flush() {
		
	}
	public void close() throws SecurityException {
		
	}
	
	public List<Exception> getUnsentExceptions() {
		return responseHandler.getUnsentExceptions();
	}
	public void setUnsentExceptions(List<Exception> list) {
		responseHandler.setUnsentExceptions(list);
		
		if (rollbar != null) {
			List<Exception> exceptions = responseHandler.getUnsentExceptions();
			responseHandler.clearExceptions();
			for (Exception ex : exceptions) {
				rewriteFilename(ex);
				responseHandler.setLastException(ex);
				try {
					rollbar.log(ex);
				} catch (Exception ex2) {
					responseHandler.addException(ex2);
				}
			}
		}
	}
	public List<LogRecord> getUnsentLogs() {
		return responseHandler.getUnsentLogs();
	}
	public void setUnsentLogs(List<LogRecord> list) {
		responseHandler.setUnsentLogs(list);
		
		if (rollbar != null) {
			List<LogRecord> records = responseHandler.getUnsentLogs();
			responseHandler.clearLogs();
			for (LogRecord record : records) {
				rewriteFilename(record);
				responseHandler.setLastLog(record);
				if (record.getThrown() != null) {
					try {
						rollbar.log(record.getThrown(), getLevel(record.getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				} else if (record.getMessage() != null) {
					try {
						rollbar.log(record.getMessage(), getLevel(record.getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				}
			}
		}
	}
	
	public boolean isLimitReached() {
		return responseHandler.limitReached;
	}
	
	//private
	private com.rollbar.payload.data.Level getLevel(Level level) {
		if (level == Level.SEVERE) {
			return com.rollbar.payload.data.Level.CRITICAL;
		}  else if (level == Level.WARNING) {
			return com.rollbar.payload.data.Level.WARNING;
		} else if (level == Level.INFO) {
			return com.rollbar.payload.data.Level.INFO;
		} else if (level == Level.CONFIG || level == Level.FINE || level == Level.FINER || level == Level.FINEST) {
			return com.rollbar.payload.data.Level.DEBUG;
		}
		
		return com.rollbar.payload.data.Level.ERROR;
	}
	
	private void handleUncaughtErrors(Thread thread) {
		if (thread == null) {
			throw new ArgumentNullException("thread");
		}
		thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable ex) {
				rewriteFilename(ex);
				responseHandler.setLastException(ex);
				try {
					rollbar.log(ex);
				} catch (Exception ex2) {
					responseHandler.addException(ex2);
				}
			}
		});
	}
	private void unhandleUncaughtErrors(Thread thread) {
		if (thread == null) {
			throw new ArgumentNullException("thread");
		}
		thread.setUncaughtExceptionHandler(null);
	}
	
	private ActionListener onResendTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			List<LogRecord> records = responseHandler.getUnsentLogs();
			responseHandler.clearLogs();
			for (LogRecord record : records) {
				rewriteFilename(record);
				responseHandler.setLastLog(record);
				if (record.getThrown() != null) {
					try {
						rollbar.log(record.getThrown(), getLevel(record.getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				} else if (record.getMessage() != null) {
					try {
						rollbar.log(record.getMessage(), getLevel(record.getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				}
			}
			List<Exception> exceptions = responseHandler.getUnsentExceptions();
			responseHandler.clearExceptions();
			for (Exception ex : exceptions) {
				rewriteFilename(ex);
				responseHandler.setLastException(ex);
				try {
					rollbar.log(ex);
				} catch (Exception ex2) {
					responseHandler.addException(ex2);
				}
			}
		}
	};
	private ActionListener onCleanupTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			errorThreads.removeIf((v) -> (!v.isAlive()));
		}
	};
	
	private void rewriteFilename(Throwable ex) {
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getFileName() == null) {
				elements[i] = new StackTraceElement(elements[i].getClassName(), elements[i].getMethodName(), "[unknown]", elements[i].getLineNumber());
			}
		}
		ex.setStackTrace(elements);
	}
	private void rewriteFilename(LogRecord record) {
		Throwable ex = record.getThrown();
		rewriteFilename(ex);
		record.setThrown(ex);
	}
}
