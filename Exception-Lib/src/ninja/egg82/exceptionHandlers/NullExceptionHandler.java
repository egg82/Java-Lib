package ninja.egg82.exceptionHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import ninja.egg82.exceptionHandlers.builders.IBuilder;
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;

public class NullExceptionHandler extends Handler implements IExceptionHandler {
	//vars
	private IObjectPool<LogRecord> logs = new DynamicObjectPool<LogRecord>();
	private IObjectPool<Exception> exceptions = new DynamicObjectPool<Exception>();
	
	//constructor
	public NullExceptionHandler() {
		
	}
	
	//public
	public void connect(IBuilder builder) {
		throw new RuntimeException("This API does not support sending exceptions.");
	}
	public void disconnect() {
		
	}
	
	public void addThread(Thread thread) {
		
	}
	public void removeThread(Thread thread) {
		
	}
	public void silentException(Exception ex) {
		exceptions.add(ex);
	}
	public void throwException(RuntimeException ex) {
		exceptions.add(ex);
		throw ex;
	}
	
	public void publish(LogRecord record) {
		logs.add(record);
	}
	public void flush() {
		
	}
	public void close() throws SecurityException {
		
	}
	
	public List<Exception> getUnsentExceptions() {
		return new ArrayList<Exception>(exceptions);
	}
	public void setUnsentExceptions(List<Exception> list) {
		exceptions.clear();
		exceptions.addAll(list);
	}
	public List<LogRecord> getUnsentLogs() {
		return new ArrayList<LogRecord>(logs);
	}
	public void setUnsentLogs(List<LogRecord> list) {
		logs.clear();
		logs.addAll(list);
	}
	
	public boolean isLimitReached() {
		return false;
	}
	
	//private
	
}
