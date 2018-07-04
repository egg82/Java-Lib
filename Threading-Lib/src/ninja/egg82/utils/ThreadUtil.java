package ninja.egg82.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.core.MinMaxScheduledThreadPoolExecutor;
import ninja.egg82.core.MinMaxThreadPoolExecutor;

public class ThreadUtil {
	//vars
	
	// Error logger
	private static final Logger logger = Logger.getLogger("ninja.egg82.utils.ThreadUtil");
	
	// Thread pools
	private static volatile MinMaxThreadPoolExecutor dynamicPool = createDynamicPool(new ThreadFactoryBuilder().setNameFormat("egg82-dynamic-%d").build());
	private static volatile MinMaxScheduledThreadPoolExecutor scheduledPool = createScheduledPool(new ThreadFactoryBuilder().setNameFormat("egg82-scheduled-%d").build());
	
	// Lock to prevent multiple simultaneous shutdowns/restarts/etc
	private static Lock objLock = new ReentrantLock();
	
	//constructor
	public ThreadUtil() {
		
	}
	
	//public
	
	/**
	 * Submit a new task to be run once
	 * 
	 * @param runnable The task to run
	 * @return The future
	 */
	public static Future<?> submit(Runnable runnable) {
		return dynamicPool.submit(new Runnable() {
			public void run() {
				// If the task throws an exception the pool will kill the thread
				try {
					runnable.run();
				} catch (Exception ex) {
					// Log the error, at least
					logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
				}
			}
		});
	}
	/**
	 * Schedule a new task to be run after a delay
	 * 
	 * @param runnable The task to run
	 * @param delayMillis The delay before running the task, in milliseconds
	 * @return The scheduled future
	 */
	public static ScheduledFuture<?> schedule(Runnable runnable, long delayMillis) {
		return scheduledPool.schedule(new Runnable() {
			public void run() {
				// If the task throws an exception the pool will kill the thread
				try {
					runnable.run();
				} catch (Exception ex) {
					// Log the error, at least
					logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
				}
			}
		}, delayMillis, TimeUnit.MILLISECONDS);
	}
	/**
	 * Schedule a new task to run after a delay, and then constantly run after a period
	 * 
	 * @param runnable The task to run
	 * @param initialDelayMillis The initial delay to wait, in milliseconds, before running the task for the first time
	 * @param periodMillis The period in milliseconds to wait before re-running the task
	 * @return The scheduled future
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelayMillis, long periodMillis) {
		return scheduledPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// If the task throws an exception the pool will completely stop trying to run this task, ever
				try {
					runnable.run();
				} catch (Exception ex) {
					// Log the error, at least
					logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
				}
			}
		}, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Sets a new name for the thread pools
	 * 
	 * @param newName The new name to set
	 */
	public static void rename(String newName) {
		objLock.lock();
		
		try {
			dynamicPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-dynamic-%d").build());
			scheduledPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-scheduled-%d").build());
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not rename thread pools.", ex);
		} finally {
			objLock.unlock();
		}
	}
	
	/**
	 * Shuts down the thread pools. Forcibly shuts them down after waiting for the specified delay
	 * 
	 * @param waitMillis The milliseconds to wait before forcibly shutting the threads down
	 */
	public static void shutdown(long waitMillis) {
		objLock.lock();
		try {
			internalShutdown(waitMillis);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not shutdown thread pools.", ex);
		} finally {
			objLock.unlock();
		}
	}
	/**
	 * Shuts down (if needed) and restarts the thread pools. Forcibly shuts them down (if needed) after waiting for the specified delay
	 * 
	 * @param shutdownWaitMillis The milliseconds to wait before forcibly shutting the threads down (if needed)
	 */
	public static void restart(long shutdownWaitMillis) {
		objLock.lock();
		
		try {
			if ((dynamicPool != null && !dynamicPool.isShutdown()) || (scheduledPool != null && !scheduledPool.isShutdown())) {
				internalShutdown(shutdownWaitMillis);
			}
			
			dynamicPool = createDynamicPool(dynamicPool.getThreadFactory());
			scheduledPool = createScheduledPool(scheduledPool.getThreadFactory());
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not restart thread pools.", ex);
		} finally {
			objLock.unlock();
		}
	}
	
	public static ExecutorService createPool(int minPoolSize, int maxPoolSize, long keepAliveMillis, ThreadFactory threadFactory) {
		return new MinMaxThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveMillis, threadFactory);
	}
	public static ScheduledExecutorService createScheduledPool(int minPoolSize, int maxPoolSize, long keepAliveMillis, ThreadFactory threadFactory) {
		return new MinMaxScheduledThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveMillis, threadFactory);
	}
	
	//private
	private static MinMaxThreadPoolExecutor createDynamicPool(ThreadFactory threadFactory) {
		// Create a pool starting at 1 and ending at the number of available processors. Kill new threads after 30s if nothing new comes in
		return new MinMaxThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 30L * 1000L, threadFactory);
	}
	private static MinMaxScheduledThreadPoolExecutor createScheduledPool(ThreadFactory threadFactory) {
		// Create a pool starting at 1 and ending at the number of available processors. Kill new threads after 120s if nothing new comes in
		return new MinMaxScheduledThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 120L * 1000L, threadFactory);
	}
	
	private static void internalShutdown(long waitMillis) {
		if (waitMillis > 0) {
			if (dynamicPool != null) {
				dynamicPool.shutdown();
			}
			if (scheduledPool != null) {
				scheduledPool.shutdown();
			}
		}
		if (dynamicPool != null) {
			try {
				if (waitMillis <= 0 || !dynamicPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
					dynamicPool.shutdownNow();
				}
			} catch (Exception ex) {
				dynamicPool.shutdownNow();
			}
		}
		if (scheduledPool != null) {
			try {
				if (waitMillis <= 0 || !scheduledPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
					scheduledPool.shutdownNow();
				}
			} catch (Exception ex) {
				dynamicPool.shutdownNow();
			}
		}
	}
}
