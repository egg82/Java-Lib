package ninja.egg82.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.Timer;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.core.NamedParameterStatement;
import ninja.egg82.core.SQLData;
import ninja.egg82.core.SQLError;
import ninja.egg82.core.SQLQueueData;
import ninja.egg82.enums.SQLType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.utils.FileUtil;

public class MySQL implements ISQL {
	//vars
	
	// Event handlers
	private final EventHandler<EventArgs> connect = new EventHandler<EventArgs>();
	private final EventHandler<EventArgs> disconnect = new EventHandler<EventArgs>();
	private final EventHandler<SQLEventArgs> data = new EventHandler<SQLEventArgs>();
	private final EventHandler<SQLEventArgs> error = new EventHandler<SQLEventArgs>();
	
	// DB connection
	private Connection conn = null;
	
	// Query backlog/queue - for queuing queries and ensuring data consistency
	private IObjectPool<SQLQueueData> backlog = new DynamicObjectPool<SQLQueueData>();
	// Object pool for storing "dead" query data - so we don't re-create a new data object for every query
	private IObjectPool<SQLQueueData> queueDataPool = new DynamicObjectPool<SQLQueueData>();
	
	// Thread pool for query thread. The thread count for actually sending queries should never exceed 1 for data consistency
	private ExecutorService threadPool = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("egg82-MySQL-%d").build());
	// A lock for the send thread. Because checking thread null and then setting the thread the new value in a multithreaded environment will cause a few issues.
	private Lock sendLock = new ReentrantLock();
	// The actual query thread, for cancellation purposes
	private volatile Future<?> sendThread = null;
	// A timer used for flushing the current query queue in case of thread failure
	private Timer backlogTimer = null;
	
	// Thread pool for reconnect thread. The thread count for actually reconnecting should never exceed 1 because that would be silly
	private ExecutorService reconnectThreadPool = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("egg82-MySQL-Reconnect-%d").build());
	// A lock for the reconnect thread. Because checking thread null and then setting the thread the new value in a multithreaded environment will cause a few issues.
	private Lock reconnectLock = new ReentrantLock();
	// The actual reconnect thread, for cancellation purposes
	private volatile Future<?> reconnectThread = null;
	
	// Connected state. Atomic because multithreading is HARD
	private AtomicBoolean connected = new AtomicBoolean(false);
	
	// Connection method for the SQL driver, since we use a class loader for the SQL connections
	private volatile static Method m = null;
	// Class loader for SQL connections. Default is system, but may change depending
	private volatile static ClassLoader loader = ClassLoader.getSystemClassLoader();
	// The jar (or in this case zip) file to download and use for dep injection in case we need it
	private static final String MYSQL_JAR = "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.45.zip";
	
	private String address = null;
	private int port = 0;
	private String user = null;
	private String pass = null;
	private String dbName = null;
	
	//constructor
	public MySQL() {
		this(null);
	}
	public MySQL(ClassLoader customLoader) {
		// Check to see if MySQL is loaded
		if (m == null || loader == null) {
			boolean good = false;
			
			// Try loading from the default system ClassLoader
			try {
				Class.forName("com.mysql.jdbc.Driver", true, loader);
				
				m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
				m.setAccessible(true);
				
				DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver", true, loader).newInstance());
				good = true;
			} catch (Exception ex) {
				
			}
			// Try loading from the custom ClassLoader supplied, if any
			if (!good && customLoader != null) {
				loader = customLoader;
				try {
					Class.forName("com.mysql.jdbc.Driver", true, loader);
					
					m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
					m.setAccessible(true);
					
					DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver", true, loader).newInstance());
					good = true;
				} catch (Exception ex) {
					
				}
			}
			// Fallback, download MySQL and inject it. Then load it from there
			if (!good) {
				File file = getMySQLFile();
				try {
					loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
					m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
					m.setAccessible(true);
					
					DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver", true, loader).newInstance());
				} catch (Exception ex2) {
					
				}
			}
		}
		
		// Set up the flush timer
		backlogTimer = new Timer(250, onBacklogTimer);
		backlogTimer.setRepeats(true);
	}
	public void finalize() {
		// Shutdown with garbage collection. This should never really happen, but hey.
		disconnect();
	}
	
	//public
	public void connect(String address, String user, String pass, String dbName) {
		connect(address, (short) 3306, user, pass, dbName);
	}
	public void connect(String address, int port, String user, String pass, String dbName) {
		if (address == null || address.isEmpty()) {
			throw new IllegalArgumentException("address cannot be null or empty.");
		}
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("port cannot be <= 0 or > 65535");
		}
		
		this.address = address;
		this.port = port;
		this.user = user;
		this.pass = pass;
		this.dbName = dbName;
		
		// Add connection properties
		Properties props = new Properties();
		props.put("user", user);
		props.put("password", pass);
		props.put("useUnicode", "true");
		props.put("characterEncoding", "UTF-8");
		props.put("failOverReadOnly", "false");
		
		// Connect to the database
		try {
			conn = (Connection) m.invoke(null, "jdbc:mysql://" + address + ":" + port + "/" + dbName, props, Class.forName("com.mysql.jdbc.Driver", true, loader));
		} catch (Exception ex) {
			throw new RuntimeException("Could not connect to database.", ex);
		}
		
		// Start the flush timer and set the connected state
		backlogTimer.start();
		connected.set(true);
		connect.invoke(this, EventArgs.EMPTY);
	}
	public void connect(String filePath) {
		throw new NotImplementedException("This database type does not support internal (file) databases.");
	}
	public void connect(String filePath, String password) {
		throw new NotImplementedException("This database type does not support internal (file) databases.");
	}
	
	public void disconnect() {
		// Set connected state to false, or return if it's already false
		if (!connected.getAndSet(false)) {
			return;
		}
		
		// Stop the flush timer
		backlogTimer.stop();
		
		// Lock (sleep the thread and wait if needed) the send thread
		sendLock.lock();
		if (sendThread != null) {
			// This should never really be "not null" if the lock works, but hey. You never know.
			sendThread.cancel(true);
			sendThread = null;
		}
		backlog.clear();
		sendLock.unlock();
		
		// Lock (sleep the thread and wait if needed) the reconnect thread
		reconnectLock.lock();
		if (reconnectThread != null) {
			// This should never really be "not null" if the lock works, but hey. You never know.
			reconnectThread.cancel(true);
			reconnectThread = null;
		}
		reconnectLock.unlock();
		
		// Close the connection gracefully
		try {
			conn.close();
		} catch (Exception ex) {
			// If this exception is ever raised something is really fucked. We'll ignore it.
		}
		
		disconnect.invoke(this, EventArgs.EMPTY);
	}
	
	public UUID query(String q, Object... queryParams) {
		if (q == null || q.isEmpty()) {
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		
		UUID u = UUID.randomUUID();
		
		// Grab a new data object if we can. Pop the last instead of the first so we don't need to re-order the entire array
		SQLQueueData queryData = queueDataPool.popLast();
		
		if (queryData == null) {
			// We ran out of queue space. We'll create one
			queryData = new SQLQueueData();
		}
		
		// Set the new data and add it to the send queue
		queryData.setQuery(q);
		queryData.setUnnamedParams(queryParams);
		queryData.setUuid(u);
		backlog.add(queryData);
		
		// Are we connected?
		if (connected.get()) {
			// Try to create a new send thread if one doesn't exist
			if (sendLock.tryLock()) {
				// Cancel thread if it's running (wait for complete)
				if (sendThread != null) {
					sendThread.cancel(false);
				}
				
				// Check the reconnect thread status
				if (!reconnectLock.tryLock()) {
					sendLock.unlock();
					return u;
				}
				reconnectLock.unlock();
				
				// Create a new send thread
				sendThread = threadPool.submit(onSendThread);
			}
		}
		
		return u;
	}
	public UUID query(String q, Map<String, Object> namedQueryParams) {
		if (q == null || q.isEmpty()) {
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		if (namedQueryParams == null) {
			throw new IllegalArgumentException("namedQueryParams cannot be null.");
		}
		
		UUID u = UUID.randomUUID();
		
		// Grab a new data object if we can. Pop the last instead of the first so we don't need to re-order the entire array
		SQLQueueData queryData = queueDataPool.popLast();
		
		if (queryData == null) {
			// We ran out of queue space. We'll create one
			queryData = new SQLQueueData();
		}
		
		// Set the new data and add it to the send queue
		queryData.setQuery(q);
		queryData.setNamedParams(namedQueryParams);
		queryData.setUuid(u);
		backlog.add(queryData);
		
		// Are we connected?
		if (connected.get()) {
			// Try to create a new send thread if one doesn't exist
			if (sendLock.tryLock()) {
				// Cancel thread if it's running (wait for complete)
				if (sendThread != null) {
					sendThread.cancel(false);
				}
				
				// Check the reconnect thread status
				if (!reconnectLock.tryLock()) {
					sendLock.unlock();
					return u;
				}
				reconnectLock.unlock();
				
				// Create a new send thread
				sendThread = threadPool.submit(onSendThread);
			}
		}
		
		return u;
	}
	
	public boolean isConnected() {
		return connected.get();
	}
	public boolean isBusy() {
		return (sendThread == null) ? false : true;
	}
	public boolean isExternal() {
		return true;
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
		return SQLType.MySQL;
	}
	
	//private
	private Runnable onSendThread = new Runnable() {
		public void run() {
			// Send the next data in the queue, if available
			sendNext();
		}
	};
	private Runnable onReconnectThread = new Runnable() {
		public void run() {
			boolean good = true;
			
			do {
				good = true;
				
				// Disconnect
				try {
					conn.close();
				} catch (Exception ex2) {
					
				}
				
				// Add connection properties
				Properties props = new Properties();
				props.put("user", user);
				props.put("password", pass);
				props.put("useUnicode", "true");
				props.put("characterEncoding", "UTF-8");
				props.put("failOverReadOnly", "false");
				
				// Reconnect
				try {
					conn = (Connection) m.invoke(null, "jdbc:mysql://" + address + ":" + port + "/" + dbName, props, Class.forName("com.mysql.jdbc.Driver", true, loader));
				} catch (Exception ex2) {
					good = false;
				}
				
				if (!good) {
					try {
						Thread.sleep(3000L);
					} catch (Exception ex) {
						
					}
				}
			} while (!good);
			
			reconnectThread = null;
			reconnectLock.unlock();
		}
	};
	private ActionListener onBacklogTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// Check connection state
			if (!connected.get()) {
				backlogTimer.stop();
				return;
			}
			
			// Check the send thread lock
			if (sendLock.tryLock()) {
				// Cancel thread if it's running (wait for complete)
				if (sendThread != null) {
					sendThread.cancel(false);
				}
				
				// Check backlog count
				if (backlog.isEmpty()) {
					sendLock.unlock();
					return;
				}
				
				// Check the reconnect thread status
				if (!reconnectLock.tryLock()) {
					sendLock.unlock();
					return;
				}
				reconnectLock.unlock();
				
				// Create a new send thread
				sendThread = threadPool.submit(onSendThread);
			}
		}
	};
	
	private void sendNext() {
		if (!connected.get()) {
			// We're no longer connected
			sendThread = null;
			sendLock.unlock();
			return;
		}
		
		// Grab the oldest data first
		SQLQueueData first = backlog.popFirst();
		if (first == null) {
			// Data is null, which means we reached the end of the queue
			sendThread = null;
			sendLock.unlock();
			return;
		}
		
		// See what type of parameters we're using (named or unnamed) and send the query off
		if (first.getNamedParams() != null) {
			queryInternal(first.getQuery(), first.getNamedParams(), first.getUuid());
		} else {
			queryInternal(first.getQuery(), first.getUnnamedParams(), first.getUuid());
		}
		
		// Clear the container object and add it back to the data pool
		first.clear();
		queueDataPool.add(first);
	}
	
	private void queryInternal(String q, Object[] parameters, UUID u) {
		// The prepared statement to use
		PreparedStatement command = null;
		
		// Try to create the statement
		try {
			command = conn.prepareStatement(q);
		} catch (Exception ex) {
			// Errored on creating the query, invoke the error method and try sending the next item in the queue
			error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		// Is this a parameterized query?
		if (parameters != null && parameters.length > 0) {
			try {
				// Loop the parameters and set them in the statement
				for (int i = 0; i < parameters.length; i++) {
					command.setObject(i + 1, parameters[i]);
				}
			} catch (Exception ex) {
				// Couldn't add parameters, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
		}
		
		// Execute the new statement
		execute(command, q, parameters, null, u);
	}
	private void queryInternal(String q, Map<String, Object> parameters, UUID u) {
		// The prepared statement to use
		NamedParameterStatement command = null;
		
		// Try to create the statement
		try {
			command = new NamedParameterStatement(conn, q);
		} catch (Exception ex) {
			// Errored on creating the query, invoke the error method and try sending the next item in the queue
			error.invoke(this, new SQLEventArgs(q, null, parameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		// Is this a parameterized query?
		if (parameters != null && parameters.size() > 0) {
			try {
				// Loop the parameters and set them in the statement
				for (Entry<String, Object> kvp : parameters.entrySet()) {
					command.setObject(kvp.getKey(), kvp.getValue());
				}
			} catch (Exception ex) {
				// Couldn't add parameters, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, null, parameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
		}
		
		// Execute the new statement
		execute(command.getPreparedStatement(), q, null, parameters, u);
	}
	private void execute(PreparedStatement command, String q, Object[] parameters, Map<String, Object> namedParameters, UUID u) {
		// Try to execute the statement
		try {
			command.execute();
		} catch (Exception ex) {
			if (ex.getClass().getSimpleName().equals("CommunicationsException")) {
				// Check connection state
				if (!connected.get()) {
					backlogTimer.stop();
					return;
				}
				
				// Grab a new data object if we can. Pop the last instead of the first so we don't need to re-order the entire array
				SQLQueueData queryData = queueDataPool.popLast();
				
				if (queryData == null) {
					// We ran out of queue space. We'll create one
					queryData = new SQLQueueData();
				}
				
				// Set the new data and add it to the send queue
				queryData.setQuery(q);
				queryData.setNamedParams(namedParameters);
				queryData.setUnnamedParams(parameters);
				queryData.setUuid(u);
				backlog.add(queryData);
				
				// Lock the reconnect thread (wait if needed)
				reconnectLock.lock();
				// Cancel thread if it's running (wait for complete)
				if (reconnectThread != null) {
					reconnectThread.cancel(false);
				}
				
				// Create a new reconnect thread
				reconnectThread = reconnectThreadPool.submit(onReconnectThread);
				
				// Clean up the send thread and return
				sendThread = null;
				sendLock.unlock();
				return;
			} else {
				// Errored on execution, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
		}
		
		// Create the return data object
		SQLData d = new SQLData();
		
		// Try to get the number of rows affected
		try {
			d.recordsAffected = command.getUpdateCount();
		} catch (Exception ex) {
			// Errored, invoke the error method and try sending the next item in the queue
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		// The result set from the query
		ResultSet results = null;
		
		// Try to get the results
		try {
			results = command.getResultSet();
		} catch (Exception ex) {
			// Errored, invoke the error method and try sending the next item in the queue
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		// Do we have a result set?
		if (results != null) {
			// The result meta from the query
			ResultSetMetaData metaData = null;
			
			// Try to get the result meta
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				// Errored, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
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
				// Errored, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
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
				// Errored, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
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
				};
			} catch (Exception ex) {
				// Errored, invoke the error method and try sending the next item in the queue
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
			
			// Add the current return data to the data object
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			// Invoke the data event and try sending the next item in the queue
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNext();
		} else {
			// Set dummy data in the return data object so nobody hits an unexpected null value
			d.columns = new String[0];
			d.data = new Object[0][];
			// Invoke the data event and try sending the next item in the queue
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNext();
		}
	}
	
	private static File getMySQLFile() {
		// The directory and file name of the downloaded jar
		File file = new File("libs" + FileUtil.DIRECTORY_SEPARATOR_CHAR + "mysql.jar");
		
		// Make sure the directory and file structure is what we expect
		if (FileUtil.pathExists(file) && !FileUtil.pathIsFile(file)) {
			FileUtil.deleteDirectory(file);
		}
		// If the file doesn't already exist, download it
		if (!FileUtil.pathExists(file)) {
			URL url = null;
			try {
				// Create the directory structure, if needed
				File d = new File(file.getParent());
	    		if (d != null) {
	    			d.mkdirs();
	    		}
				
	    		// Download the zip
				url = new URL(MYSQL_JAR);
				ZipInputStream zip = new ZipInputStream(url.openStream());
				ZipEntry entry = null;
				
				// Iterate the zip file and look for the MySQL jar
				do {
					entry = zip.getNextEntry();
					if (entry.getName().equals("mysql-connector-java-5.1.45\\mysql-connector-java-5.1.45-bin.jar") || entry.getName().equals("mysql-connector-java-5.1.45/mysql-connector-java-5.1.45-bin.jar")) {
						break;
					}
				} while (entry != null);
				
				if (entry == null) {
					// We didn't find the MySQL jar
					zip.close();
					return file;
				}
				
				// Create a new FileOutputStream to extract the compressed jar to
				OutputStream out = new FileOutputStream(file);
				
				// Write the jar file to disk
				byte[] buffer = new byte[1024];
				int len = zip.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = zip.read(buffer);
				};
				
				// Cleanup
				out.close();
				zip.close();
			} catch (Exception ex) {
				
			}
		}
		
		return file;
	}
}
