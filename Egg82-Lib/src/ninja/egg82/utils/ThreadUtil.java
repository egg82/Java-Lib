package ninja.egg82.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.core.MinMaxScheduledThreadPoolExecutor;

public class ThreadUtil {
	//vars
	
	// Error logger
	private static final Logger logger = Logger.getLogger("ninja.egg82.utils.ThreadUtil");
	
	// Thread pools
	private static ThreadPoolExecutor dynamicPool = null;
	private static ScheduledExecutorService singlePool = null;
	private static ThreadFactory singleThreadFactory = new ThreadFactoryBuilder().setNameFormat("egg82-single_scheduled-%d").build();
	private static MinMaxScheduledThreadPoolExecutor scheduledPool = null;
	
	// Thread queue for the dynamic pool
	private static BlockingQueue<Runnable> dynamicQueue = new ArrayBlockingQueue<Runnable>(250);
	
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
		if (dynamicPool == null) {
			dynamicPool = createDynamicPool(new ThreadFactoryBuilder().setNameFormat("egg82-dynamic-%d").build());
		}
		
		// We don't care much about killed threads here, since they auto-restart
		return dynamicPool.submit(runnable);
	}
	/**
	 * Schedule a new task to be run after a delay
	 * 
	 * @param runnable The task to run
	 * @param delayMillis The delay before running the task, in milliseconds
	 * @return The scheduled future
	 */
	public static ScheduledFuture<?> schedule(Runnable runnable, long delayMillis) {
		if (singlePool == null) {
			singlePool = Executors.newSingleThreadScheduledExecutor(singleThreadFactory);
		}
		
		return singlePool.schedule(new Runnable() {
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
		if (scheduledPool == null) {
			scheduledPool = createScheduledPool(new ThreadFactoryBuilder().setNameFormat("egg82-scheduled-%d").build());
		}
		
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
		if (dynamicPool == null) {
			dynamicPool = createDynamicPool(new ThreadFactoryBuilder().setNameFormat(newName + "-dynamic-%d").build());
		} else {
			dynamicPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-dynamic-%d").build());
		}
		singleThreadFactory = new ThreadFactoryBuilder().setNameFormat(newName + "-single_scheduled-%d").build();
		if (singlePool != null) {
			singlePool.shutdown();
		}
		singlePool = Executors.newSingleThreadScheduledExecutor(singleThreadFactory);
		if (scheduledPool == null) {
			scheduledPool = createScheduledPool(new ThreadFactoryBuilder().setNameFormat(newName + "-scheduled-%d").build());
		} else {
			scheduledPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-scheduled-%d").build());
		}
	}
	
	/**
	 * Shuts down the thread pools. Forcibly shuts them down after waiting for the specified delay
	 * 
	 * @param waitMillis The milliseconds to wait before forcibly shutting the threads down
	 */
	public static void shutdown(long waitMillis) {
		try {
			if (waitMillis > 0) {
				if (dynamicPool != null) {
					dynamicPool.shutdown();
				}
				if (singlePool != null) {
					singlePool.shutdown();
				}
				if (scheduledPool != null) {
					scheduledPool.shutdown();
				}
			}
			if (dynamicPool != null) {
				if (waitMillis <= 0 || !dynamicPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
					dynamicPool.shutdownNow();
				}
			}
			if (singlePool != null) {
				if (waitMillis <= 0 || !singlePool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
					singlePool.shutdownNow();
				}
			}
			if (scheduledPool != null) {
				if (waitMillis <= 0 || !scheduledPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
					scheduledPool.shutdownNow();
				}
			}
		} catch (Exception ex) {
			
		}
	}
	/**
	 * Shuts down (if needed) and restarts the thread pools. Forcibly shuts them down (if needed) after waiting for the specified delay
	 * 
	 * @param shutdownWaitMillis The milliseconds to wait before forcibly shutting the threads down (if needed)
	 */
	public static void restart(long shutdownWaitMillis) {
		if ((dynamicPool != null && !dynamicPool.isShutdown()) || (singlePool != null && !singlePool.isShutdown()) || (scheduledPool != null && !scheduledPool.isShutdown())) {
			shutdown(shutdownWaitMillis);
		}
		
		dynamicPool = createDynamicPool(dynamicPool.getThreadFactory());
		singlePool = Executors.newSingleThreadScheduledExecutor(singleThreadFactory);
		scheduledPool = createScheduledPool(scheduledPool.getThreadFactory());
	}
	
	public static ScheduledExecutorService createScheduledPool(int minPoolSize, int maxPoolSize, long keepAliveMillis, ThreadFactory threadFactory) {
		return new MinMaxScheduledThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveMillis, threadFactory);
	}
	
	//private
	private static ThreadPoolExecutor createDynamicPool(ThreadFactory threadFactory) {
		// Create a pool starting at 1 and ending at the number of available processors. Kill new threads after 30s if nothing new comes in
		ThreadPoolExecutor retVal = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 30L * 1000L, TimeUnit.MILLISECONDS, dynamicQueue);
		// Allow the core threads to terminate just like the non-core threads
		retVal.allowCoreThreadTimeOut(true);
		
		// Fill the backlog if needed
		retVal.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				try {
					executor.getQueue().put(r);
				} catch (Exception ex) {
					
				}
			}
		});
		
		// Set the factory
		retVal.setThreadFactory(threadFactory);
		
		return retVal;
	}
	private static MinMaxScheduledThreadPoolExecutor createScheduledPool(ThreadFactory threadFactory) {
		// Create a pool starting at 1 and ending at the number of available processors. Kill new threads after 120s if nothing new comes in
		return new MinMaxScheduledThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 120L * 1000L, threadFactory);
	}
}
