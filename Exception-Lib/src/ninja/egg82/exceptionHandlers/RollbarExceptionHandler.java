package ninja.egg82.exceptionHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rollbar.Rollbar;
import com.rollbar.payload.data.Person;

import ninja.egg82.core.LoggingRollbarResponseHandler;
import ninja.egg82.exceptionHandlers.builders.IBuilder;
import ninja.egg82.exceptions.ArgumentNullException;
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;
import ninja.egg82.patterns.tuples.Pair;

public class RollbarExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private Rollbar rollbar = null;
	private LoggingRollbarResponseHandler responseHandler = new LoggingRollbarResponseHandler();
	
	private ScheduledExecutorService threadPool = null;
	private IObjectPool<Thread> errorThreads = new DynamicObjectPool<Thread>();
	
	//constructor
	public RollbarExceptionHandler() {
		Logger.getLogger("ninja.egg82.core.PasswordHasher").addHandler(this);
		Logger.getLogger("ninja.egg82.utils.ThreadUtil").addHandler(this);
		Logger.getLogger("ninja.egg82.patterns.events.EventHandler").addHandler(this);
	}
	
	//public
	public void connect(IBuilder builder, String threadName) {
		String[] params = builder.getParams();
		if (params == null || params.length != 4) {
			throw new IllegalArgumentException("params must have a length of 4. Use ninja.egg82.exceptionHandlers.builders.RollbarBuilder");
		}
		rollbar = new Rollbar(params[0], params[1]).codeVersion(params[2]).responseHandler(responseHandler).person(new Person(params[3]));
		handleUncaughtErrors(Thread.currentThread());
		
		List<Pair<LogRecord, Integer>> records = responseHandler.getUnsentLogs();
		responseHandler.clearLogs();
		for (Pair<LogRecord, Integer> record : records) {
			rewriteFilename(record.getLeft());
			responseHandler.setLastLog(record.getLeft());
			responseHandler.setTries(record.getRight());
			if (record.getLeft().getThrown() != null) {
				try {
					rollbar.log(record.getLeft().getThrown(), getLevel(record.getLeft().getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			} else if (record.getLeft().getMessage() != null) {
				try {
					rollbar.log(record.getLeft().getMessage(), getLevel(record.getLeft().getLevel()));
				} catch (Exception ex) {
					responseHandler.addException(ex);
				}
			}
		}
		List<Pair<Exception, Integer>> exceptions = responseHandler.getUnsentExceptions();
		responseHandler.clearExceptions();
		for (Pair<Exception, Integer> ex : exceptions) {
			rewriteFilename(ex.getLeft());
			responseHandler.setLastException(ex.getLeft());
			responseHandler.setTries(ex.getRight());
			try {
				rollbar.log(ex.getLeft());
			} catch (Exception ex2) {
				responseHandler.addException(ex2);
			}
		}
		
		threadPool = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(threadName + "-Rollbar_Exception-%d").build());
		threadPool.scheduleAtFixedRate(onCleanupThread, 60L * 1000L, 60L * 1000L, TimeUnit.MILLISECONDS);
		threadPool.scheduleAtFixedRate(onResendThread, 10L * 60L * 1000L, 10L * 60L * 1000L, TimeUnit.MILLISECONDS);
	}
	public void disconnect() {
		threadPool.shutdownNow();
		for (Thread t : errorThreads) {
			unhandleUncaughtErrors(t);
		}
		errorThreads.clear();
	}
	
	public void addThread(Thread thread) {
		if (rollbar != null && !errorThreads.contains(thread)) {
			handleUncaughtErrors(thread);
			errorThreads.add(thread);
		}
	}
	public void removeThread(Thread thread) {
		if (rollbar != null && errorThreads.remove(thread)) {
			unhandleUncaughtErrors(thread);
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
		ArrayList<Exception> retVal = new ArrayList<Exception>();
		List<Pair<Exception, Integer>> logs = responseHandler.getUnsentExceptions();
		logs.forEach((v) -> {
			retVal.add(v.getLeft());
		});
		return retVal;
	}
	public void setUnsentExceptions(List<Exception> list) {
		responseHandler.setUnsentExceptions(list);
		
		if (rollbar != null) {
			List<Pair<Exception, Integer>> exceptions = responseHandler.getUnsentExceptions();
			responseHandler.clearExceptions();
			for (Pair<Exception, Integer> ex : exceptions) {
				rewriteFilename(ex.getLeft());
				responseHandler.setLastException(ex.getLeft());
				responseHandler.setTries(ex.getRight());
				try {
					rollbar.log(ex.getLeft());
				} catch (Exception ex2) {
					responseHandler.addException(ex2);
				}
			}
		}
	}
	public List<LogRecord> getUnsentLogs() {
		ArrayList<LogRecord> retVal = new ArrayList<LogRecord>();
		List<Pair<LogRecord, Integer>> logs = responseHandler.getUnsentLogs();
		logs.forEach((v) -> {
			retVal.add(v.getLeft());
		});
		return retVal;
	}
	public void setUnsentLogs(List<LogRecord> list) {
		responseHandler.setUnsentLogs(list);
		
		if (rollbar != null) {
			List<Pair<LogRecord, Integer>> records = responseHandler.getUnsentLogs();
			responseHandler.clearLogs();
			for (Pair<LogRecord, Integer> record : records) {
				rewriteFilename(record.getLeft());
				responseHandler.setLastLog(record.getLeft());
				responseHandler.setTries(record.getRight());
				if (record.getLeft().getThrown() != null) {
					try {
						rollbar.log(record.getLeft().getThrown(), getLevel(record.getLeft().getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				} else if (record.getLeft().getMessage() != null) {
					try {
						rollbar.log(record.getLeft().getMessage(), getLevel(record.getLeft().getLevel()));
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
				if (!(ex instanceof Exception)) {
					ex = new Exception(ex);
				}
				responseHandler.setLastException((Exception) ex);
				responseHandler.setTries(0);
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
	
	private Runnable onResendThread = new Runnable() {
		public void run() {
			List<Pair<LogRecord, Integer>> records = responseHandler.getUnsentLogs();
			responseHandler.clearLogs();
			for (Pair<LogRecord, Integer> record : records) {
				rewriteFilename(record.getLeft());
				responseHandler.setLastLog(record.getLeft());
				responseHandler.setTries(record.getRight());
				if (record.getLeft().getThrown() != null) {
					try {
						rollbar.log(record.getLeft().getThrown(), getLevel(record.getLeft().getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				} else if (record.getLeft().getMessage() != null) {
					try {
						rollbar.log(record.getLeft().getMessage(), getLevel(record.getLeft().getLevel()));
					} catch (Exception ex) {
						responseHandler.addException(ex);
					}
				}
			}
			List<Pair<Exception, Integer>> exceptions = responseHandler.getUnsentExceptions();
			responseHandler.clearExceptions();
			for (Pair<Exception, Integer> ex : exceptions) {
				rewriteFilename(ex.getLeft());
				responseHandler.setLastException(ex.getLeft());
				responseHandler.setTries(ex.getRight());
				try {
					rollbar.log(ex.getLeft());
				} catch (Exception ex2) {
					responseHandler.addException(ex2);
				}
			}
		}
	};
	private Runnable onCleanupThread = new Runnable() {
		public void run() {
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
		if (ex != null) {
			rewriteFilename(ex);
			record.setThrown(ex);
		}
	}
}
