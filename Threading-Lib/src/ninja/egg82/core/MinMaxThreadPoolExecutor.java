package ninja.egg82.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MinMaxThreadPoolExecutor implements ExecutorService {
    // vars

    // Backing thread pools
    private ThreadPoolExecutor core = null;
    private ThreadPoolExecutor extra = null;

    // Max threads in the extra pool
    private int extraMax = 0;

    // constructor
    public MinMaxThreadPoolExecutor(int min, int max, long keepAliveMillis) {
        this(min, max, keepAliveMillis, Executors.defaultThreadFactory());
    }

    public MinMaxThreadPoolExecutor(int min, int max, long keepAliveMillis, ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new IllegalArgumentException("threadFactory cannot be null.");
        }
        if (keepAliveMillis <= 0) {
            throw new IllegalArgumentException("keepAliveMillis cannot be 0.");
        }
        if (min == 0 && max == 0) {
            throw new IllegalArgumentException("min and max cannot be 0.");
        }

        extraMax = max - min;
        if (extraMax < 0) {
            throw new IllegalArgumentException("max cannot be < min.");
        }

        if (min > 0) {
            core = new ThreadPoolExecutor(min, Integer.MAX_VALUE, keepAliveMillis, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
        }
        if (extraMax > 0) {
            extra = createExtra(keepAliveMillis, threadFactory);
        }
    }

    // public
    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (extra != null) {
            extra.setKeepAliveTime(time, unit);
        }
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (core != null) {
            core.setThreadFactory(threadFactory);
        }
        if (extra != null) {
            extra.setThreadFactory(threadFactory);
        }
    }

    public ThreadFactory getThreadFactory() {
        return (core != null) ? core.getThreadFactory() : extra.getThreadFactory();
    }

    public void shutdown() {
        if (core != null) {
            core.shutdown();
        }
        if (extra != null) {
            extra.shutdown();
        }
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> tasks = new ArrayList<Runnable>();

        if (core != null) {
            tasks.addAll(core.shutdownNow());
        }
        if (extra != null) {
            tasks.addAll(extra.shutdownNow());
        }

        return tasks;
    }

    public boolean isShutdown() {
        return ((core == null || core.isShutdown()) && (extra == null || extra.isShutdown())) ? true : false;
    }

    public boolean isTerminated() {
        return ((core == null || core.isTerminated()) && (extra == null || extra.isTerminated())) ? true : false;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return ((core == null || core.awaitTermination(timeout, unit)) && (extra == null || extra.awaitTermination(timeout, unit))) ? true : false;
    }

    public <T> Future<T> submit(Callable<T> task) {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra
                if (core.getActiveCount() >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(extra.getCorePoolSize() + 1);
                    }
                    return extra.submit(getHandlerWrapper(task));
                } else {
                    // Core is not maxed out, submit to core
                    return core.submit(task);
                }
            } else {
                // We don't have extra, submit to core
                return core.submit(task);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(extra.getCorePoolSize() + 1);
            }
            return extra.submit(getHandlerWrapper(task));
        }
    }

    public <T> Future<T> submit(Runnable task, T result) {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra
                if (core.getActiveCount() >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(extra.getCorePoolSize() + 1);
                    }
                    return extra.submit(getHandlerWrapper(task), result);
                } else {
                    // Core is not maxed out, submit to core
                    return core.submit(task, result);
                }
            } else {
                // We don't have extra, submit to core
                return core.submit(task, result);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(extra.getCorePoolSize() + 1);
            }
            return extra.submit(getHandlerWrapper(task), result);
        }
    }

    public Future<?> submit(Runnable task) {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra
                if (core.getActiveCount() >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(extra.getCorePoolSize() + 1);
                    }
                    return extra.submit(getHandlerWrapper(task));
                } else {
                    // Core is not maxed out, submit to core
                    return core.submit(task);
                }
            } else {
                // We don't have extra, submit to core
                return core.submit(task);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(extra.getCorePoolSize() + 1);
            }
            return extra.submit(getHandlerWrapper(task));
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra

                // Get the core pool active count
                int coreActiveCount = core.getActiveCount();

                if (coreActiveCount >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
                    }
                    return extra.invokeAll(tasks);
                } else if (coreActiveCount >= core.getCorePoolSize() - tasks.size()) {
                    // Core doesn't have enough room to take the full amount. Submit to both

                    // Create a new (splittable) list
                    List<Callable<T>> list = new ArrayList<Callable<T>>();
                    list.addAll(tasks);

                    List<Future<T>> retVal = new ArrayList<Future<T>>();

                    // The number of tasks core can't take
                    int remainingTasks = core.getCorePoolSize() - coreActiveCount;

                    // Split the list and add to core
                    retVal.addAll(core.invokeAll(list.subList(0, remainingTasks)));
                    if (extra.getCorePoolSize() < extraMax && extra.getActiveCount() >= extra.getCorePoolSize()) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + remainingTasks));
                    }
                    // Add the remainder to extra
                    retVal.addAll(extra.invokeAll(getHandlerWrapper(list.subList(remainingTasks + 1, tasks.size() - 1))));

                    return retVal;
                } else {
                    // Core is not maxed out, submit to core
                    return core.invokeAll(tasks);
                }
            } else {
                // We don't have extra, submit to core. Submit to extra
                return core.invokeAll(tasks);
            }
        } else {
            // We have extra, but no core
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
            }
            return extra.invokeAll(getHandlerWrapper(tasks));
        }
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra

                // Get the core pool active count
                int coreActiveCount = core.getActiveCount();

                if (coreActiveCount >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
                    }
                    return extra.invokeAll(tasks);
                } else if (coreActiveCount >= core.getCorePoolSize() - tasks.size()) {
                    // Core doesn't have enough room to take the full amount. Submit to both

                    // Create a new (splittable) list
                    List<Callable<T>> list = new ArrayList<Callable<T>>();
                    list.addAll(tasks);

                    List<Future<T>> retVal = new ArrayList<Future<T>>();

                    // The number of tasks core can't take
                    int remainingTasks = core.getCorePoolSize() - coreActiveCount;

                    // Split the list and add to core
                    retVal.addAll(core.invokeAll(list.subList(0, remainingTasks)));
                    if (extra.getCorePoolSize() < extraMax && extra.getActiveCount() >= extra.getCorePoolSize()) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + remainingTasks));
                    }
                    // Add the remainder to extra
                    retVal.addAll(extra.invokeAll(getHandlerWrapper(list.subList(remainingTasks + 1, tasks.size() - 1))));

                    return retVal;
                } else {
                    // Core is not maxed out, submit to core
                    return core.invokeAll(tasks);
                }
            } else {
                // We don't have extra, submit to core. Submit to extra
                return core.invokeAll(tasks);
            }
        } else {
            // We have extra, but no core
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
            }
            return extra.invokeAll(getHandlerWrapper(tasks));
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra

                // Get the core pool active count
                int coreActiveCount = core.getActiveCount();

                if (coreActiveCount >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
                    }
                    return extra.invokeAny(tasks);
                } else if (coreActiveCount >= core.getCorePoolSize() - tasks.size()) {
                    // Core doesn't have enough room to take the full amount. Submit to both

                    // Create a new (splittable) list
                    List<Callable<T>> list = new ArrayList<Callable<T>>();
                    list.addAll(tasks);

                    T retVal = null;

                    // The number of tasks core can't take
                    int remainingTasks = core.getCorePoolSize() - coreActiveCount;

                    // Split the list and add to core
                    retVal = core.invokeAny(list.subList(0, remainingTasks));
                    if (retVal == null) {
                        // No return value yet, submit the remaining tasks to extra
                        if (extra.getCorePoolSize() < extraMax && extra.getActiveCount() >= extra.getCorePoolSize()) {
                            // Extra is entirely active, but not maxed out
                            extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + remainingTasks));
                        }
                        // Add the remainder to extra
                        retVal = extra.invokeAny(getHandlerWrapper(list.subList(remainingTasks + 1, tasks.size() - 1)));
                    }

                    return retVal;
                } else {
                    // Core is not maxed out, submit to core
                    return core.invokeAny(tasks);
                }
            } else {
                // We don't have extra, submit to core
                return core.invokeAny(tasks);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
            }
            return extra.invokeAny(getHandlerWrapper(tasks));
        }
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra

                // Get the core pool active count
                int coreActiveCount = core.getActiveCount();

                if (coreActiveCount >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
                    }
                    return extra.invokeAny(tasks);
                } else if (coreActiveCount >= core.getCorePoolSize() - tasks.size()) {
                    // Core doesn't have enough room to take the full amount. Submit to both

                    // Create a new (splittable) list
                    List<Callable<T>> list = new ArrayList<Callable<T>>();
                    list.addAll(tasks);

                    T retVal = null;

                    // The number of tasks core can't take
                    int remainingTasks = core.getCorePoolSize() - coreActiveCount;

                    // Split the list and add to core
                    retVal = core.invokeAny(list.subList(0, remainingTasks));
                    if (retVal == null) {
                        // No return value yet, submit the remaining tasks to extra
                        if (extra.getCorePoolSize() < extraMax && extra.getActiveCount() >= extra.getCorePoolSize()) {
                            // Extra is entirely active, but not maxed out
                            extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + remainingTasks));
                        }
                        // Add the remainder to extra
                        retVal = extra.invokeAny(getHandlerWrapper(list.subList(remainingTasks + 1, tasks.size() - 1)));
                    }

                    return retVal;
                } else {
                    // Core is not maxed out, submit to core
                    return core.invokeAny(tasks);
                }
            } else {
                // We don't have extra, submit to core
                return core.invokeAny(tasks);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(Math.min(extraMax, extra.getCorePoolSize() + tasks.size()));
            }
            return extra.invokeAny(getHandlerWrapper(tasks));
        }
    }

    public void execute(Runnable command) {
        if (core != null) {
            // We have a core
            if (extra != null) {
                // We have extra
                if (core.getActiveCount() >= core.getCorePoolSize()) {
                    // Core is maxed out
                    if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                        // Extra is entirely active, but not maxed out
                        extra.setCorePoolSize(extra.getCorePoolSize() + 1);
                    }
                    extra.execute(getHandlerWrapper(command));
                } else {
                    // Core is not maxed out, submit to core
                    core.execute(command);
                }
            } else {
                // We don't have extra, submit to core
                core.execute(command);
            }
        } else {
            // We have extra, but no core. Submit to extra
            if (extra.getActiveCount() >= extra.getCorePoolSize() && extra.getCorePoolSize() < extraMax) {
                // Extra is entirely active, but not maxed out
                extra.setCorePoolSize(extra.getCorePoolSize() + 1);
            }
            extra.execute(getHandlerWrapper(command));
        }
    }

    // private
    private ThreadPoolExecutor createExtra(long keepAliveMillis, ThreadFactory threadFactory) {
        // Create a pool with a size of 1
        ThreadPoolExecutor retVal = new ThreadPoolExecutor(1, Integer.MAX_VALUE, keepAliveMillis, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new FifoThreadFactory(threadFactory));
        // Kill new threads after keepAliveMillis if nothing new comes in
        retVal.setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
        // Allow the core threads to terminate
        retVal.allowCoreThreadTimeOut(true);

        return retVal;
    }

    private <T> Callable<T> getHandlerWrapper(Callable<T> task) {
        return new Callable<T>() {
            public T call() throws Exception {
                T result = null;
                Exception lastEx = null;
                try {
                    result = task.call();
                } catch (Exception ex) {
                    lastEx = ex;
                }

                if (extra.getActiveCount() >= extra.getCorePoolSize()) {
                    // No need for extra threads
                    extra.setCorePoolSize(extra.getCorePoolSize() - 1);
                }

                if (lastEx != null) {
                    throw new RuntimeException(lastEx);
                }

                return result;
            }
        };
    }

    private Runnable getHandlerWrapper(Runnable task) {
        return new Runnable() {
            public void run() {
                Exception lastEx = null;
                try {
                    task.run();
                } catch (Exception ex) {
                    lastEx = ex;
                }

                if (extra.getActiveCount() >= extra.getCorePoolSize()) {
                    // No need for extra threads
                    extra.setCorePoolSize(extra.getCorePoolSize() - 1);
                }

                if (lastEx != null) {
                    throw new RuntimeException(lastEx);
                }
            }
        };
    }

    private <T> Collection<? extends Callable<T>> getHandlerWrapper(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> retVal = new ArrayList<Callable<T>>();

        for (Callable<T> task : tasks) {
            retVal.add(getHandlerWrapper(task));
        }

        return retVal;
    }
}
