package ninja.egg82.analytics.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogRecord;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rollbar.Rollbar;
import com.rollbar.payload.data.Level;
import com.rollbar.sender.RollbarResponse;
import com.rollbar.sender.RollbarResponseCode;
import com.rollbar.sender.RollbarResponseHandler;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.DynamicConcurrentSet;
import ninja.egg82.concurrent.IConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.utils.ThreadUtil;

public class RetryRollbarResponseHandler implements RollbarResponseHandler {
	//vars
	private Rollbar rollbar = null;
	
	private volatile boolean limitReached = false;
	private AtomicBoolean ready = new AtomicBoolean(false);
	
	private IConcurrentSet<String> allExceptions = new DynamicConcurrentSet<String>();
	
	private IConcurrentDeque<ExceptionLogContainer> queuedLogs = new DynamicConcurrentDeque<ExceptionLogContainer>();
	private volatile ExceptionLogContainer currentLog = null;
	private IConcurrentDeque<ExceptionLogContainer> failedLogs = new DynamicConcurrentDeque<ExceptionLogContainer>();
	
	private AtomicInteger currentCount = new AtomicInteger(0);
	
	private ScheduledExecutorService threadPool = null;
	
	//constructor
	public RetryRollbarResponseHandler(String threadName) {
		threadPool = ThreadUtil.createScheduledPool(0, Runtime.getRuntime().availableProcessors(), 120L * 1000L, new ThreadFactoryBuilder().setNameFormat(threadName + "-Rollbar-%d").build());
		threadPool.scheduleAtFixedRate(onBacklogThread, 1000L, 1000L, TimeUnit.MILLISECONDS);
		ready.set(true);
	}
	
	//public
	public void setRollbar(Rollbar rollbar) {
		this.rollbar = rollbar;
	}
	
	public void sendException(Throwable ex) {
		setFilename(ex);
		
		String exString = null;
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			ex.printStackTrace(pw);
			exString = sw.toString();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		
		if (!allExceptions.add(exString)) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(ex));
		
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	public void sendRecord(LogRecord record) {
		setFilename(record);
		
		if (record.getThrown() != null) {
			String exString = null;
			try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
				record.getThrown().printStackTrace(pw);
				exString = sw.toString();
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
			
			if (!allExceptions.add(exString)) {
				return;
			}
		} else if (record.getMessage() != null) {
			if (!allExceptions.add(record.getMessage())) {
				return;
			}
		}
		
		queuedLogs.add(new ExceptionLogContainer(record));
		
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	public void sendMessage(String message, LogLevel level) {
		if (!allExceptions.add(message)) {
			return;
		}
		
		queuedLogs.add(new ExceptionLogContainer(message, level));
		
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	public void handleResponse(RollbarResponse response) {
		if (response.isSuccessful()) {
			limitReached = false;
			currentLog = null;
			currentCount.set(0);
			sendNext();
			return;
		}
		
		if (response.statusCode() == RollbarResponseCode.TooManyRequests) {
			limitReached = true;
			failedLogs.add(currentLog);
			currentLog = null;
			currentCount.set(0);
			sendNext();
		} else {
			limitReached = false;
			if (currentCount.incrementAndGet() >= 3) {
				failedLogs.add(currentLog);
				currentLog = null;
				currentCount.set(0);
				sendNext();
			} else {
				log(currentLog);
			}
		}
	}
	
	public boolean isLimitReached() {
		return limitReached;
	}
	
	public List<ExceptionLogContainer> getUnsentLogs() {
		List<ExceptionLogContainer> total = new ArrayList<ExceptionLogContainer>();
		
		for (ExceptionLogContainer container : failedLogs) {
			total.add(container);
		}
		total.add(currentLog);
		for (ExceptionLogContainer container : queuedLogs) {
			total.add(container);
		}
		
		while (total.remove(null)) {
			// Just iteratively removing null values
		}
		
		return total;
	}
	
	//private
	private Runnable onSendThread = new Runnable() {
		public void run() {
			if (!ready.getAndSet(false)) {
				return;
			}
			
			sendNext();
		}
	};
	private Runnable onBacklogThread = new Runnable() {
		public void run() {
			if (!ready.getAndSet(false) || queuedLogs.isEmpty()) {
				return;
			}
			
			sendNext();
		}
	};
	
	private void sendNext() {
		ExceptionLogContainer first = queuedLogs.pollFirst();
		if (first == null) {
			ready.set(true);
			return;
		}
		
		log(first);
	}
	
	private void log(ExceptionLogContainer log) {
		currentLog = log;
		
		if (log.getEx() != null) {
			logException(log.getEx(), log.getLevel());
		} else if (log.getRecord() != null) {
			if (log.getRecord().getThrown() != null) {
				logException(log.getRecord().getThrown(), log.getLevel());
			} else if (log.getRecord().getMessage() != null) {
				logMessage(log.getRecord().getMessage(), log.getLevel());
			}
		} else if (log.getMessage() != null) {
			logMessage(log.getMessage(), log.getLevel());
		}
	}
	private void logException(Throwable ex, LogLevel level) {
		try {
			if (level == LogLevel.CRITICAL) {
				rollbar.log(ex, Level.CRITICAL);
			} else if (level == LogLevel.ERROR) {
				rollbar.log(ex, Level.ERROR);
			} else if (level == LogLevel.WARNING) {
				rollbar.log(ex, Level.WARNING);
			} else if (level == LogLevel.INFO) {
				rollbar.log(ex, Level.INFO);
			} else if (level == LogLevel.DEBUG) {
				rollbar.log(ex, Level.DEBUG);
			} else {
				rollbar.log(ex, Level.ERROR);
			}
		} catch (Exception ex2) {
			try {
				rollbar.log(ex2);
			} catch (Exception ex3) {
				ex3.printStackTrace();
			}
		}
	}
	private void logMessage(String message, LogLevel level) {
		try {
			if (level == LogLevel.CRITICAL) {
				rollbar.log(message, Level.CRITICAL);
			} else if (level == LogLevel.ERROR) {
				rollbar.log(message, Level.ERROR);
			} else if (level == LogLevel.WARNING) {
				rollbar.log(message, Level.WARNING);
			} else if (level == LogLevel.INFO) {
				rollbar.log(message, Level.INFO);
			} else if (level == LogLevel.DEBUG) {
				rollbar.log(message, Level.DEBUG);
			} else {
				rollbar.log(message, Level.ERROR);
			}
		} catch (Exception ex2) {
			try {
				rollbar.log(ex2);
			} catch (Exception ex3) {
				ex3.printStackTrace();
			}
		}
	}
	
	private void setFilename(Throwable ex) {
		StackTraceElement[] elements = ex.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getFileName() == null || elements[i].getFileName().trim().length() == 0) {
				elements[i] = new StackTraceElement(elements[i].getClassName(), elements[i].getMethodName(), "[unknown]", elements[i].getLineNumber());
			}
		}
		ex.setStackTrace(elements);
	}
	private void setFilename(LogRecord record) {
		Throwable ex = record.getThrown();
		if (ex != null) {
			setFilename(ex);
			record.setThrown(ex);
		}
	}
}
