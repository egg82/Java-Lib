package ninja.egg82.analytics.core;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ExceptionLogContainer {
	//vars
	private Throwable ex = null;
	private LogRecord record = null;
	private String message = null;
	private LogLevel level = null;
	
	//constructor
	public ExceptionLogContainer(Throwable ex) {
		this.ex = ex;
		level = (ex instanceof Error) ? LogLevel.CRITICAL : LogLevel.ERROR;
	}
	public ExceptionLogContainer(LogRecord record) {
		this.record = record;
		
		if (record.getLevel() == Level.SEVERE) {
			this.level = (record.getThrown() != null && record.getThrown() instanceof Error) ? LogLevel.CRITICAL : LogLevel.ERROR;
		} else if (record.getLevel() == Level.WARNING) {
			this.level = LogLevel.WARNING;
		} else if (record.getLevel() == Level.INFO) {
			this.level = LogLevel.INFO;
		} else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER || record.getLevel() == Level.FINEST) {
			this.level = LogLevel.DEBUG;
		} else {
			this.level = LogLevel.ERROR;
		}
	}
	public ExceptionLogContainer(String message, LogLevel level) {
		this.message = message;
		this.level = level;
	}
	
	//public
	public Throwable getEx() {
		return ex;
	}
	public LogRecord getRecord() {
		return record;
	}
	public String getMessage() {
		return message;
	}
	public LogLevel getLevel() {
		return level;
	}
	
	//private
	
}
