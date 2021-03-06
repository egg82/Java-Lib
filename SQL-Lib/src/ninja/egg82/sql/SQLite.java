package ninja.egg82.sql;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.FixedConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;
import ninja.egg82.core.NamedParameterStatement;
import ninja.egg82.core.SQLData;
import ninja.egg82.core.SQLError;
import ninja.egg82.core.SQLFileUtil;
import ninja.egg82.core.SQLQueueData;
import ninja.egg82.enums.BaseSQLType;
import ninja.egg82.enums.SQLType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.utils.ThreadUtil;

public class SQLite implements ISQL {
	//vars
	
	// Event handlers
	private final EventHandler<EventArgs> connect = new EventHandler<EventArgs>();
	private final EventHandler<EventArgs> disconnect = new EventHandler<EventArgs>();
	private final EventHandler<SQLEventArgs> data = new EventHandler<SQLEventArgs>();
	private final EventHandler<SQLEventArgs> error = new EventHandler<SQLEventArgs>();
	
	// free DB connection pool, where connections are taken from
	private IConcurrentDeque<Connection> freeConnections = null;
	// used DB connection pool, where connections are added while in use
	private IConcurrentDeque<Connection> usedConnections = new DynamicConcurrentDeque<Connection>();
	
	// Query backlog/queue - for queuing queries and ensuring data consistency
	private IConcurrentDeque<SQLQueueData> backlog = new DynamicConcurrentDeque<SQLQueueData>();
	
	// Thread pool for query threads. Pool size determined by constructor
	private ScheduledExecutorService threadPool = null;
	// A lock that, when locked, tells the current send threads to wait for the current blocking query to finish
	private Lock parallelLock = new ReentrantLock();
	// Name given to the thread pool
	private String threadName = null;
	
	// Connected state. Atomic because multithreading is HARD
	private AtomicBoolean connected = new AtomicBoolean(false);
	
	// Double-lock, preventing race conditions in a multi-threaded environment
	private static Lock objLock = new ReentrantLock();
	// Connection method for the SQL driver, since we use a class loader for the SQL connections
	private volatile static Method m = null;
	// Class loader for SQL connections. Default is system, but may change depending
	private volatile static ClassLoader loader = ClassLoader.getSystemClassLoader();
	// The jar file to download and use for dep injection in case we need it
	private static final String SQLITE_JAR = "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.23.1.jar";
	
	private File file = null;
	
	//constructor
	public SQLite(int numConnections, String threadName) {
		this(numConnections, threadName, null);
	}
	public SQLite(int numConnections, String threadName, ClassLoader customLoader) {
		if (numConnections < 1) {
			numConnections = 1;
		}
		freeConnections = new FixedConcurrentDeque<Connection>(numConnections);
		
		this.threadName = threadName;
		
		// Check to see if SQLite is loaded
		objLock.lock();
		try {
			if (m == null || loader == null) {
				boolean good = false;
				
				// Try loading from the default system ClassLoader
				try {
					Class.forName("org.sqlite.JDBC", true, loader);
					
					m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
					m.setAccessible(true);
					
					DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC", true, loader).newInstance());
					good = true;
				} catch (Exception ex) {
					
				}
				// Try loading from the custom ClassLoader supplied, if any
				if (!good && customLoader != null) {
					loader = customLoader;
					try {
						Class.forName("org.sqlite.JDBC", true, loader);
						
						m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
						m.setAccessible(true);
						
						DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC", true, loader).newInstance());
						good = true;
					} catch (Exception ex) {
						
					}
				}
				// Fallback, download SQLite and inject it. Then load it from there
				if (!good) {
					File file = getSQLiteFile();
					try {
						loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
						m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
						m.setAccessible(true);
						
						DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC", true, loader).newInstance());
					} catch (Exception ex2) {
						
					}
				}
			}
		} catch (Exception ex) {
			
		} finally {
			objLock.unlock();
		}
	}
	
	//public
	public void connect(String address, String user, String pass, String dbName) {
		throw new NotImplementedException("This database type does not support external (non-file) databases.");
	}
	public void connect(String address, int port, String user, String pass, String dbName) {
		throw new NotImplementedException("This database type does not support external (non-file) databases.");
	}
	public void connect(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			throw new IllegalArgumentException("filePath cannot be null or empty.");
		}
		
		// Disconnect if already connected
		disconnect();
		
		this.file = new File(filePath);
		
		// Create the directory and file structure, if needed
		if (!SQLFileUtil.pathExists(file)) {
			try {
				SQLFileUtil.createFile(file);
			} catch (Exception ex) {
				throw new RuntimeException("Could not create database file.", ex);
			}
		} else {
			if (!SQLFileUtil.pathIsFile(file)) {
				throw new RuntimeException("filePath is not a valid file.");
			}
		}
		
		// Connect to the database
		while (freeConnections.getRemainingCapacity() > 0) {
			try {
				freeConnections.add((Connection) m.invoke(null, "jdbc:sqlite:" + file.getAbsolutePath(), new Properties(), Class.forName("org.sqlite.JDBC", true, loader)));
			} catch (Exception ex) {
				throw new RuntimeException("Could not connect to database.", ex);
			}
		}
		
		// Create the thread pool. Why here instead of the constructor? Because we call shutdown() on this pool in disconnect
		threadPool = ThreadUtil.createScheduledPool(1, freeConnections.size() * 2, 120L * 1000L, new ThreadFactoryBuilder().setNameFormat(threadName + "-SQlite-%d").build());
		
		// Start the flush timer and set the connected state
		threadPool.scheduleAtFixedRate(onBacklogThread, 250L, 250L, TimeUnit.MILLISECONDS);
		connected.set(true);
		connect.invoke(this, EventArgs.EMPTY);
	}
	
	@SuppressWarnings("resource")
	public void disconnect() {
		// Set connected state to false, or return if it's already false
		if (!connected.getAndSet(false)) {
			return;
		}
		
		// Shutdown the send threads gracefully, then not-so-gracefully after 15 seconds
		try {
			threadPool.shutdown();
			if (!threadPool.awaitTermination(15000L, TimeUnit.MILLISECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (Exception ex) {
			
		}
		backlog.clear();
		
		// Kill connections in use (hopefully zero)
		while (!usedConnections.isEmpty()) {
			Connection conn = usedConnections.pollLast();
			
			if (conn != null) {
				// Close the connection gracefully
				try {
					conn.close();
				} catch (Exception ex) {
					// If this exception is ever raised something is really fucked. We'll ignore it.
				}
			} else {
				break;
			}
		}
		// Kill connections not in use
		while (!freeConnections.isEmpty()) {
			Connection conn = freeConnections.pollLast();
			
			if (conn != null) {
				// Close the connection gracefully
				try {
					conn.close();
				} catch (Exception ex) {
					// If this exception is ever raised something is really fucked. We'll ignore it.
				}
			} else {
				break;
			}
		}
		
		disconnect.invoke(this, EventArgs.EMPTY);
	}
	
	public UUID query(String q, Object... queryParams) {
		return query(q, false, queryParams);
	}
	public UUID parallelQuery(String q, Object... queryParams) {
		return query(q, true, queryParams);
	}
	public UUID query(String q, Map<String, Object> namedQueryParams) {
		return query(q, false, namedQueryParams);
	}
	public UUID parallelQuery(String q, Map<String, Object> namedQueryParams) {
		return query(q, true, namedQueryParams);
	}
	
	public boolean isConnected() {
		return connected.get();
	}
	public boolean isBusy() {
		return (usedConnections.size() > 0) ? true : false;
	}
	
	public EventHandler<EventArgs> onConnect() {
		return connect;
	}
	public EventHandler<EventArgs> onDisconnect() {
		return disconnect;
	}
	public EventHandler<SQLEventArgs> onData() {
		return data;
	}
	public EventHandler<SQLEventArgs> onError() {
		return error;
	}
	
	public SQLType getType() {
		return BaseSQLType.SQLite;
	}
	
	//private
	private UUID query(String q, boolean parallel, Object... queryParams) {
		if (q == null || q.isEmpty()) {
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		
		UUID u = UUID.randomUUID();
		
		// Grab a new data object and add it to the send queue
		SQLQueueData queryData = new SQLQueueData(u, q, queryParams, parallel);
		backlog.add(queryData);
		
		// Are we connected?
		if (connected.get()) {
			// Add a new query task
			threadPool.submit(onSendThread);
		}
		
		return u;
	}
	private UUID query(String q, boolean parallel, Map<String, Object> namedQueryParams) {
		if (q == null || q.isEmpty()) {
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		if (namedQueryParams == null) {
			throw new IllegalArgumentException("namedQueryParams cannot be null.");
		}
		
		UUID u = UUID.randomUUID();
		
		// Grab a new data object and add it to the send queue
		SQLQueueData queryData = new SQLQueueData(u, q, namedQueryParams, parallel);
		backlog.add(queryData);
		
		// Are we connected?
		if (connected.get()) {
			// Add a new query task
			threadPool.submit(onSendThread);
		}
		
		return u;
	}
	
	@SuppressWarnings("resource")
	private Runnable onSendThread = new Runnable() {
		public void run() {
			// Grab a new connection from the free pool. We grab the first to ensure rotation
			Connection conn = freeConnections.pollFirst();
			if (conn == null) {
				// No free connections left
				return;
			}
			usedConnections.add(conn);
			
			// Send the next data in the queue, if available
			sendNext(conn);
		}
	};
	@SuppressWarnings("resource")
	private Runnable onBacklogThread = new Runnable() {
		public void run() {
			// Check connection state and backlog count
			if (!connected.get() || backlog.isEmpty()) {
				return;
			}
			
			// Grab a new connection from the free pool. We grab the first to ensure rotation
			Connection conn = freeConnections.pollFirst();
			if (conn == null) {
				// No free connections left
				return;
			}
			usedConnections.add(conn);
			
			// Send the next data in the queue, if available
			sendNext(conn);
		}
	};
	
	@SuppressWarnings("resource")
	private void sendNext(Connection conn) {
		if (!connected.get()) {
			// We're no longer connected
			usedConnections.remove(conn);
			freeConnections.add(conn);
			return;
		}
		
		// Try to take the parallel lock
		if (!parallelLock.tryLock()) {
			usedConnections.remove(conn);
			freeConnections.add(conn);
			return;
		}
		
		// Grab the oldest data first
		SQLQueueData first = backlog.pollFirst();
		if (first == null) {
			// Data is null, which means we reached the end of the queue
			usedConnections.remove(conn);
			freeConnections.add(conn);
			parallelLock.unlock();
			return;
		}
		
		if (first.getParallel()) {
			// Parallel connection. Release the parallel lock
			parallelLock.unlock();
		}
		
		// See what type of parameters we're using (named or unnamed) and send the query off
		if (first.getNamedParams() != null) {
			// The prepared statement to use
			NamedParameterStatement command = null;
			
			// Try to create the statement
			try {
				command = new NamedParameterStatement(conn, first.getQuery());
			} catch (Exception ex) {
				// Errored on creating the query, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(first.getQuery(), null, first.getNamedParams(), new SQLError(ex), new SQLData(), first.getUuid()));
				if (first.getParallel()) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			
			// Is this a parameterized query?
			if (first.getNamedParams() != null && first.getNamedParams().size() > 0) {
				try {
					// Loop the parameters and set them in the statement
					for (Entry<String, Object> kvp : first.getNamedParams().entrySet()) {
						command.setObject(kvp.getKey(), kvp.getValue());
					}
				} catch (Exception ex) {
					// Couldn't add parameters, invoke the error method and try sending the next item in the queue
					error.invoke(this, new SQLEventArgs(first.getQuery(), null, first.getNamedParams(), new SQLError(ex), new SQLData(), first.getUuid()));
					if (first.getParallel()) {
						parallelLock.unlock();
					}
					sendNext(conn);
					return;
				}
			}
			
			boolean parallel = first.getParallel();
			String query = first.getQuery();
			Map<String, Object> namedParams = first.getNamedParams();
			UUID uuid = first.getUuid();
			
			// Execute the new statement
			execute(conn, command.getPreparedStatement(), parallel, query, null, namedParams, uuid);
		} else {
			// The prepared statement to use
			PreparedStatement command = null;
			
			// Try to create the statement
			try {
				command = conn.prepareStatement(first.getQuery());
			} catch (Exception ex) {
				// Errored on creating the query, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(first.getQuery(), first.getUnnamedParams(), null, new SQLError(ex), new SQLData(), first.getUuid()));
				if (first.getParallel()) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			
			// Is this a parameterized query?
			if (first.getUnnamedParams() != null && first.getUnnamedParams().length > 0) {
				try {
					// Loop the parameters and set them in the statement
					for (int i = 0; i < first.getUnnamedParams().length; i++) {
						command.setObject(i + 1, first.getUnnamedParams()[i]);
					}
				} catch (Exception ex) {
					// Couldn't add parameters, invoke the error method and try sending the next item in the queue
					error.invoke(this, new SQLEventArgs(first.getQuery(), first.getUnnamedParams(), null, new SQLError(ex), new SQLData(), first.getUuid()));
					if (first.getParallel()) {
						parallelLock.unlock();
					}
					sendNext(conn);
					return;
				}
			}
			
			// Grab the data BEFORE we clear the container
			boolean parallel = first.getParallel();
			String query = first.getQuery();
			Object[] params = first.getUnnamedParams();
			UUID uuid = first.getUuid();
			
			// Execute the new statement
			execute(conn, command, parallel, query, params, null, uuid);
		}
	}
	
	@SuppressWarnings("resource")
	private void execute(Connection conn, PreparedStatement command, boolean isParallel, String q, Object[] parameters, Map<String, Object> namedParameters, UUID u) {
		// Try to execute the statement
		boolean hasResultSet = false;
		try {
			hasResultSet = command.execute();
		} catch (Exception ex) {
			if (ex.getClass().getSimpleName().equals("CommunicationsException") || ex.getClass().getSimpleName().equals("EOFException") || contains("CommunicationsException", ex.getCause())) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Check connection state
				if (!connected.get()) {
					return;
				}
				
				// Grab a new data object and add it to the beginning of the send queue (preserving order)
				SQLQueueData queryData = new SQLQueueData(u, q, namedParameters, parameters, isParallel);
				backlog.addFirst(queryData);
				
				// Unlock the parallel lock if it's currently locked, BEFORE we create a new send thread
				if (!isParallel) {
					parallelLock.unlock();
				}
				
				// Create a new send thread
				threadPool.submit(onSendThread);
				
				// Reconnect on this thread
				usedConnections.remove(conn);
				conn = reconnect(conn);
				freeConnections.add(conn);
			} else {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored on execution, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
			}
			return;
		}
		
		// Create the return data object
		SQLData d = new SQLData();
		
		// Try to get the number of rows affected
		if (!hasResultSet) {
			try {
				d.recordsAffected = command.getUpdateCount();
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
		}
		
		// The result set from the query
		ResultSet results = null;
		
		// Try to get the results
		if (hasResultSet) {
			try {
				results = command.getResultSet();
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
		}
		
		// Do we have a result set?
		if (results != null) {
			// The result meta from the query
			ResultSetMetaData metaData = null;
			
			// Try to get the result meta
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			
			// Create column data
			ArrayList<String> tColumns = new ArrayList<String>();
			
			// Try to get the number of columns in the results and the names of those columns
			try {
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					tColumns.add(metaData.getColumnName(i));
				}
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			d.columns = tColumns.toArray(new String[0]);
			
			// Create table data
			ArrayList<Object[]> tData = new ArrayList<Object[]>();
			
			// Try to get the number of rows and the data returned
			try {
				// Loop the rows
				while (results.next()) {
					// Create a row with the size of the data returned
					Object[] tVals = new Object[tColumns.size()];
					// Iterate cells and add them to the current return row
					for (int i = 0; i < tColumns.size(); i++) {
						tVals[i] = results.getObject(i + 1);
					}
					// Add the row to the current return data
					tData.add(tVals);
				}
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			
			// Get the next X number of results, if (and as) requested. SQL queries return "pages" and we'll loop through all of them
			try {
				// Loop through the rest of the pages
				while(command.getMoreResults()) {
					// Get the result set and add data as we did above
					results = command.getResultSet();
					while (results.next()) {
						Object[] tVals = new Object[tColumns.size()];
						for (int i = 0; i < tColumns.size(); i++) {
							tVals[i] = results.getObject(i + 1);
						}
						tData.add(tVals);
					}
				}
			} catch (Exception ex) {
				// Release resources
				try {
					command.close();
				} catch (Exception ex2) {
					
				}
				
				// Errored, invoke the error method and try sending the next item in the queue
				threadPool.submit(new Runnable() {
					public void run() {
						error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
					}
				});
				if (!isParallel) {
					parallelLock.unlock();
				}
				sendNext(conn);
				return;
			}
			
			// Add the current return data to the data object
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			// Release resources
			try {
				command.close();
			} catch (Exception ex2) {
				
			}
			
			// Invoke the data event and try sending the next item in the queue
			threadPool.submit(new Runnable() {
				public void run() {
					data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
				}
			});
			if (!isParallel) {
				parallelLock.unlock();
			}
			sendNext(conn);
		} else {
			// Release resources
			try {
				command.close();
			} catch (Exception ex2) {
				
			}
			
			// Set dummy data in the return data object so nobody hits an unexpected null value
			d.columns = new String[0];
			d.data = new Object[0][];
			// Invoke the data event and try sending the next item in the queue
			threadPool.submit(new Runnable() {
				public void run() {
					data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
				}
			});
			if (!isParallel) {
				parallelLock.unlock();
			}
			sendNext(conn);
		}
	}
	
	private Connection reconnect(Connection conn) {
		// Disconnect
		try {
			conn.close();
		} catch (Exception ex2) {
			
		}
		
		boolean good = true;
		do {
			good = true;
			
			// Reconnect
			try {
				conn = (Connection) m.invoke(null, "jdbc:sqlite:" + file.getAbsolutePath(), new Properties(), Class.forName("org.sqlite.JDBC", true, loader));
			} catch (Exception ex2) {
				good = false;
			}
			
			if (!good) {
				try {
					Thread.sleep(1000L);
				} catch (Exception ex) {
					
				}
			}
		} while (!good);
		
		return conn;
	}
	
	private static boolean contains(String needle, Throwable cause) {
		if (cause == null) {
			return false;
		}
		if (cause.getClass().getSimpleName().equals(needle)) {
			return true;
		}
		return contains(needle, cause.getCause());
	}
	
	private static File getSQLiteFile() {
		// The directory and file name of the downloaded jar
		File file = new File(new File("libs"), "sqlite.jar");
		
		// Make sure the directory and file structure is what we expect
		if (SQLFileUtil.pathExists(file) && !SQLFileUtil.pathIsFile(file)) {
			SQLFileUtil.deleteDirectory(file);
		}
		// If the file doesn't already exist, download it
		if (!SQLFileUtil.pathExists(file)) {
			URL url = null;
			try {
				File d = new File(file.getParent());
	    		d.mkdirs();
				
	    		// Download the jar
				url = new URL(SQLITE_JAR);
				try (InputStream in = url.openStream()) {
					// Write the jar file to disk
					Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (Exception ex) {
				
			}
		}
		
		return file;
	}
}
