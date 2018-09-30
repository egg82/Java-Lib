package ninja.egg82.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.core.MinMaxScheduledThreadPoolExecutor;
import ninja.egg82.core.MinMaxThreadPoolExecutor;

public class ThreadUtil {
    // vars

    // Error logger
    private static final Logger logger = Logger.getLogger("ninja.egg82.utils.ThreadUtil");

    // Thread pools
    private static volatile MinMaxThreadPoolExecutor dynamicPool = createDynamicPool(new ThreadFactoryBuilder().setNameFormat("egg82-dynamic-%d").build());
    private static volatile MinMaxScheduledThreadPoolExecutor scheduledPool = createScheduledPool(new ThreadFactoryBuilder().setNameFormat("egg82-scheduled-%d").build());

    private static volatile ThreadFactory infiniteDynamicFactory = new ThreadFactoryBuilder().setNameFormat("egg82-infinite-dynamic-%d").build();
    private static volatile ExecutorService infiniteDynamicPool = Executors.newCachedThreadPool(infiniteDynamicFactory);
    private static volatile ThreadFactory infiniteScheduledFactory = new ThreadFactoryBuilder().setNameFormat("egg82-infinite-scheduled-%d").build();
    private static volatile ScheduledExecutorService infiniteScheduledPool = Executors.newScheduledThreadPool(0, infiniteScheduledFactory);

    // Lock to prevent multiple simultaneous shutdowns/restarts/etc
    private static Lock objLock = new ReentrantLock();
    // Lock to prevent submitting of new runnables while pools are shut down
    private static ReadWriteLock threadLock = new ReentrantReadWriteLock();

    // constructor
    public ThreadUtil() {

    }

    // public

    /**
     * Submit a new task to be run once
     * 
     * @param runnable The task to run
     * @return The future
     */
    public static Future<?> submit(Runnable runnable) throws RejectedExecutionException {
        threadLock.readLock().lock();

        if (dynamicPool.isShutdown()) {
            throw new RejectedExecutionException("pool is shut down.");
        }

        Future<?> retVal = dynamicPool.submit(new Runnable() {
            public void run() {
                // If the task throws an exception the pool will kill the thread
                try {
                    runnable.run();
                } catch (RejectedExecutionException ex) {
                    // Ignored
                } catch (Exception ex) {
                    // Log the error, at least
                    logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
                }
            }
        });

        threadLock.readLock().unlock();

        return retVal;
    }
    /**
     * Submit a new (infinitely-running) task to be run once
     * 
     * @param runnable The infinitely-running task to run
     * @return The future
     */
    public static Future<?> submitInfinite(Runnable runnable) throws RejectedExecutionException {
        threadLock.readLock().lock();

        if (infiniteDynamicPool.isShutdown()) {
            throw new RejectedExecutionException("pool is shut down.");
        }

        Future<?> retVal = infiniteDynamicPool.submit(new Runnable() {
            public void run() {
                // If the task throws an exception the pool will kill the thread
                try {
                    runnable.run();
                } catch (RejectedExecutionException ex) {
                    // Ignored
                } catch (Exception ex) {
                    // Log the error, at least
                    logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
                }
            }
        });

        threadLock.readLock().unlock();

        return retVal;
    }

    /**
     * Schedule a new task to be run after a delay
     * 
     * @param runnable    The task to run
     * @param delayMillis The delay before running the task, in milliseconds
     * @return The scheduled future
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delayMillis) throws RejectedExecutionException {
        threadLock.readLock().lock();

        if (scheduledPool.isShutdown()) {
            throw new RejectedExecutionException("pool is shut down.");
        }

        ScheduledFuture<?> retVal = scheduledPool.schedule(new Runnable() {
            public void run() {
                // If the task throws an exception the pool will kill the thread
                try {
                    runnable.run();
                } catch (RejectedExecutionException ex) {
                    // Ignored
                } catch (Exception ex) {
                    // Log the error, at least
                    logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
                }
            }
        }, delayMillis, TimeUnit.MILLISECONDS);

        threadLock.readLock().unlock();

        return retVal;
    }
    /**
     * Schedule a new (infinitely-running) task to be run after a delay
     * 
     * @param runnable    The infinitely-running task to run
     * @param delayMillis The delay before running the task, in milliseconds
     * @return The scheduled future
     */
    public static ScheduledFuture<?> scheduleInfinite(Runnable runnable, long delayMillis) throws RejectedExecutionException {
        threadLock.readLock().lock();

        if (infiniteScheduledPool.isShutdown()) {
            throw new RejectedExecutionException("pool is shut down.");
        }

        ScheduledFuture<?> retVal = infiniteScheduledPool.schedule(new Runnable() {
            public void run() {
                // If the task throws an exception the pool will kill the thread
                try {
                    runnable.run();
                } catch (RejectedExecutionException ex) {
                    // Ignored
                } catch (Exception ex) {
                    // Log the error, at least
                    logger.log(Level.SEVERE, "Could not run scheduled task.", ex);
                }
            }
        }, delayMillis, TimeUnit.MILLISECONDS);

        threadLock.readLock().unlock();

        return retVal;
    }

    /**
     * Sets a new name for the thread pools
     * 
     * @param newName The new name to set
     */
    public static void rename(String newName) {
        objLock.lock();
        threadLock.writeLock().lock();

        try {
            if (dynamicPool.isShutdown()) {
                dynamicPool = createDynamicPool(new ThreadFactoryBuilder().setNameFormat(newName + "-dynamic-%d").build());
            } else {
                dynamicPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-dynamic-%d").build());
            }
            infiniteDynamicPool.shutdownNow();
            infiniteDynamicFactory = new ThreadFactoryBuilder().setNameFormat(newName + "-infinite-dynamic-%d").build();
            infiniteDynamicPool = Executors.newCachedThreadPool(infiniteDynamicFactory);
            if (scheduledPool.isShutdown()) {
                scheduledPool = createScheduledPool(new ThreadFactoryBuilder().setNameFormat(newName + "-scheduled-%d").build());
            } else {
                scheduledPool.setThreadFactory(new ThreadFactoryBuilder().setNameFormat(newName + "-scheduled-%d").build());
            }
            infiniteScheduledPool.shutdownNow();
            infiniteScheduledFactory = new ThreadFactoryBuilder().setNameFormat(newName + "-infinite-scheduled-%d").build();
            infiniteScheduledPool = Executors.newScheduledThreadPool(0, infiniteScheduledFactory);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not rename thread pools.", ex);
        } finally {
            threadLock.writeLock().unlock();
            objLock.unlock();
        }
    }
    /**
     * Shuts down the thread pools. Forcibly shuts them down after waiting for the
     * specified delay
     * 
     * @param waitMillis The milliseconds to wait before forcibly shutting the
     *                   threads down
     */
    public static void shutdown(long waitMillis) {
        objLock.lock();
        threadLock.writeLock().lock();
        try {
            internalShutdown(waitMillis);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not shutdown thread pools.", ex);
        } finally {
            threadLock.writeLock().unlock();
            objLock.unlock();
        }
    }
    /**
     * Shuts down (if needed) and restarts the thread pools. Forcibly shuts them
     * down (if needed) after waiting for the specified delay
     * 
     * @param shutdownWaitMillis The milliseconds to wait before forcibly shutting
     *                           the threads down (if needed)
     */
    public static void restart(long shutdownWaitMillis) {
        objLock.lock();
        threadLock.writeLock().lock();

        try {
            if ((dynamicPool != null && !dynamicPool.isShutdown()) || (infiniteDynamicPool != null && !infiniteDynamicPool.isShutdown()) || (scheduledPool != null && !scheduledPool.isShutdown())
                || (infiniteScheduledPool != null && !infiniteScheduledPool.isShutdown())) {
                internalShutdown(shutdownWaitMillis);
            }

            dynamicPool = createDynamicPool(dynamicPool.getThreadFactory());
            infiniteDynamicPool = Executors.newCachedThreadPool(infiniteDynamicFactory);
            scheduledPool = createScheduledPool(scheduledPool.getThreadFactory());
            infiniteScheduledPool = Executors.newScheduledThreadPool(0, infiniteScheduledFactory);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not restart thread pools.", ex);
        } finally {
            threadLock.writeLock().unlock();
            objLock.unlock();
        }
    }

    public static ExecutorService createPool(int minPoolSize, int maxPoolSize, long keepAliveMillis, ThreadFactory threadFactory) {
        return new MinMaxThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveMillis, threadFactory);
    }
    public static ScheduledExecutorService createScheduledPool(int minPoolSize, int maxPoolSize, long keepAliveMillis, ThreadFactory threadFactory) {
        return new MinMaxScheduledThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveMillis, threadFactory);
    }

    // private
    private static MinMaxThreadPoolExecutor createDynamicPool(ThreadFactory threadFactory) {
        // Create a pool starting at 1 and ending at the number of available processors.
        // Kill new threads after 30s if nothing new comes in
        return new MinMaxThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 30L * 1000L, threadFactory);
    }
    private static MinMaxScheduledThreadPoolExecutor createScheduledPool(ThreadFactory threadFactory) {
        // Create a pool starting at 1 and ending at the number of available processors.
        // Kill new threads after 120s if nothing new comes in
        return new MinMaxScheduledThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(), 120L * 1000L, threadFactory);
    }

    private static void internalShutdown(long waitMillis) {
        if (waitMillis > 0) {
            if (dynamicPool != null) {
                dynamicPool.shutdown();
            }
            if (infiniteDynamicPool != null) {
                infiniteDynamicPool.shutdown();
            }
            if (scheduledPool != null) {
                scheduledPool.shutdown();
            }
            if (infiniteScheduledPool != null) {
                infiniteScheduledPool.shutdown();
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
        if (infiniteDynamicPool != null) {
            try {
                if (waitMillis <= 0 || !infiniteDynamicPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
                    infiniteDynamicPool.shutdownNow();
                }
            } catch (Exception ex) {
                infiniteDynamicPool.shutdownNow();
            }
        }
        if (scheduledPool != null) {
            try {
                if (waitMillis <= 0 || !scheduledPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
                    scheduledPool.shutdownNow();
                }
            } catch (Exception ex) {
                scheduledPool.shutdownNow();
            }
        }
        if (infiniteScheduledPool != null) {
            try {
                if (waitMillis <= 0 || !infiniteScheduledPool.awaitTermination(waitMillis, TimeUnit.MILLISECONDS)) {
                    infiniteScheduledPool.shutdownNow();
                }
            } catch (Exception ex) {
                infiniteScheduledPool.shutdownNow();
            }
        }
    }
}
