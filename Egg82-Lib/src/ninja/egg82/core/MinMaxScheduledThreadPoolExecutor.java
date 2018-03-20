package ninja.egg82.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ninja.egg82.exceptions.ArgumentNullException;

public class MinMaxScheduledThreadPoolExecutor implements ScheduledExecutorService {
	//vars
	private ScheduledThreadPoolExecutor core = null;
	private ScheduledThreadPoolExecutor extra = null;
	
	//constructor
	public MinMaxScheduledThreadPoolExecutor(int min, int max, long keepAliveMillis) {
		this(min, max, keepAliveMillis, Executors.defaultThreadFactory());
	}
	public MinMaxScheduledThreadPoolExecutor(int min, int max, long keepAliveMillis, ThreadFactory threadFactory) {
		if (threadFactory == null) {
			throw new ArgumentNullException("threadFactory");
		}
		if (keepAliveMillis <= 0) {
			throw new IllegalArgumentException("keepAliveMillis cannot be 0.");
		}
		if (min == 0 && max == 0) {
			throw new IllegalArgumentException("min and max cannot be 0.");
		}
		
		int extraThreads = max - min;
		if (extraThreads < 0) {
			throw new IllegalArgumentException("max cannot be < min.");
		}
		
		if (min > 0) {
			core = createCore(min, threadFactory);
		}
		if (extraThreads > 0) {
			extra = createExtra(extraThreads, keepAliveMillis, threadFactory);
		}
	}
	
	//public
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
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.submit(task);
				} else {
					return core.submit(task);
				}
			} else {
				return core.submit(task);
			}
		}
		if (extra != null) {
			return extra.submit(task);
		}
		return null;
	}
	public <T> Future<T> submit(Runnable task, T result) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.submit(task, result);
				} else {
					return core.submit(task, result);
				}
			} else {
				return core.submit(task, result);
			}
		}
		if (extra != null) {
			return extra.submit(task, result);
		}
		return null;
	}
	public Future<?> submit(Runnable task) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.submit(task);
				} else {
					return core.submit(task);
				}
			} else {
				return core.submit(task);
			}
		}
		if (extra != null) {
			return extra.submit(task);
		}
		return null;
	}
	
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.invokeAll(tasks);
				}
				
				if (core.getActiveCount() >= core.getCorePoolSize() - tasks.size()) {
					List<Callable<T>> list = new ArrayList<Callable<T>>();
					list.addAll(tasks);
					
					List<Future<T>> retVal = new ArrayList<Future<T>>();
					
					int remainingTasks = core.getCorePoolSize() - core.getActiveCount();
					
					retVal.addAll(core.invokeAll(list.subList(0, remainingTasks)));
					retVal.addAll(extra.invokeAll(list.subList(remainingTasks + 1, tasks.size() - 1)));
					
					return retVal;
				} else {
					return core.invokeAll(tasks);
				}
			} else {
				return core.invokeAll(tasks);
			}
		}
		if (extra != null) {
			return extra.invokeAll(tasks);
		}
		return null;
	}
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.invokeAll(tasks, timeout, unit);
				}
				
				if (core.getActiveCount() >= core.getCorePoolSize() - tasks.size()) {
					List<Callable<T>> list = new ArrayList<Callable<T>>();
					list.addAll(tasks);
					
					List<Future<T>> retVal = new ArrayList<Future<T>>();
					
					int remainingTasks = core.getCorePoolSize() - core.getActiveCount();
					
					retVal.addAll(core.invokeAll(list.subList(0, remainingTasks), timeout, unit));
					retVal.addAll(extra.invokeAll(list.subList(remainingTasks + 1, tasks.size() - 1), timeout, unit));
					
					return retVal;
				} else {
					return core.invokeAll(tasks, timeout, unit);
				}
			} else {
				return core.invokeAll(tasks, timeout, unit);
			}
		}
		if (extra != null) {
			return extra.invokeAll(tasks, timeout, unit);
		}
		return null;
	}
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.invokeAny(tasks);
				}
				
				if (core.getActiveCount() >= core.getCorePoolSize() - tasks.size()) {
					List<Callable<T>> list = new ArrayList<Callable<T>>();
					list.addAll(tasks);
					
					T retVal = null;
					
					int remainingTasks = core.getCorePoolSize() - core.getActiveCount();
					
					retVal = core.invokeAny(list.subList(0, remainingTasks));
					if (retVal == null) {
						retVal = extra.invokeAny(list.subList(remainingTasks + 1, tasks.size() - 1));
					}
					
					return retVal;
				} else {
					return core.invokeAny(tasks);
				}
			} else {
				return core.invokeAny(tasks);
			}
		}
		if (extra != null) {
			return extra.invokeAny(tasks);
		}
		return null;
	}
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.invokeAny(tasks, timeout, unit);
				}
				
				if (core.getActiveCount() >= core.getCorePoolSize() - tasks.size()) {
					List<Callable<T>> list = new ArrayList<Callable<T>>();
					list.addAll(tasks);
					
					T retVal = null;
					
					int remainingTasks = core.getCorePoolSize() - core.getActiveCount();
					
					retVal = core.invokeAny(list.subList(0, remainingTasks), timeout, unit);
					if (retVal == null) {
						retVal = extra.invokeAny(list.subList(remainingTasks + 1, tasks.size() - 1), timeout, unit);
					}
					
					return retVal;
				} else {
					return core.invokeAny(tasks, timeout, unit);
				}
			} else {
				return core.invokeAny(tasks, timeout, unit);
			}
		}
		if (extra != null) {
			return extra.invokeAny(tasks, timeout, unit);
		}
		return null;
	}
	
	public void execute(Runnable command) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					extra.execute(command);
				} else {
					core.execute(command);
				}
			} else {
				core.execute(command);
			}
		}
		if (extra != null) {
			extra.execute(command);
		}
	}
	
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.schedule(command, delay, unit);
				} else {
					return core.schedule(command, delay, unit);
				}
			} else {
				return core.schedule(command, delay, unit);
			}
		}
		if (extra != null) {
			return extra.schedule(command, delay, unit);
		}
		return null;
	}
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.schedule(callable, delay, unit);
				} else {
					return core.schedule(callable, delay, unit);
				}
			} else {
				return core.schedule(callable, delay, unit);
			}
		}
		if (extra != null) {
			return extra.schedule(callable, delay, unit);
		}
		return null;
	}
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.scheduleAtFixedRate(command, initialDelay, period, unit);
				} else {
					return core.scheduleAtFixedRate(command, initialDelay, period, unit);
				}
			} else {
				return core.scheduleAtFixedRate(command, initialDelay, period, unit);
			}
		}
		if (extra != null) {
			return extra.scheduleAtFixedRate(command, initialDelay, period, unit);
		}
		return null;
	}
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		if (core != null) {
			if (extra != null) {
				if (core.getQueue().size() > 0 || core.getActiveCount() >= core.getCorePoolSize()) {
					return extra.scheduleWithFixedDelay(command, initialDelay, delay, unit);
				} else {
					return core.scheduleWithFixedDelay(command, initialDelay, delay, unit);
				}
			} else {
				return core.scheduleWithFixedDelay(command, initialDelay, delay, unit);
			}
		}
		if (extra != null) {
			return extra.scheduleWithFixedDelay(command, initialDelay, delay, unit);
		}
		return null;
	}
	
	//private
	private ScheduledThreadPoolExecutor createCore(int size, ThreadFactory threadFactory) {
		// Create a pool with the specified size
		ScheduledThreadPoolExecutor retVal = new ScheduledThreadPoolExecutor(size);
		// Pre-start threads
		retVal.prestartAllCoreThreads();
		
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
	private ScheduledThreadPoolExecutor createExtra(int size, long keepAliveMillis, ThreadFactory threadFactory) {
		// Create a pool with the specified size
		ScheduledThreadPoolExecutor retVal = new ScheduledThreadPoolExecutor(size);
		// Kill new threads after keepAliveMillis if nothing new comes in
		retVal.setKeepAliveTime(keepAliveMillis, TimeUnit.MILLISECONDS);
		// Allow the core threads to terminate
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
}
