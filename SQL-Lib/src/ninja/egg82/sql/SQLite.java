package ninja.egg82.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.apache.commons.lang.NotImplementedException;

import ninja.egg82.core.NamedParameterStatement;
import ninja.egg82.core.SQLData;
import ninja.egg82.core.SQLError;
import ninja.egg82.enums.SQLType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.DynamicObjectPool;
import ninja.egg82.patterns.IObjectPool;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Triplet;
import ninja.egg82.utils.FileUtil;

public class SQLite implements ISQL {
	//vars
	private final EventHandler<EventArgs> connect = new EventHandler<EventArgs>();
	private final EventHandler<EventArgs> disconnect = new EventHandler<EventArgs>();
	private final EventHandler<SQLEventArgs> data = new EventHandler<SQLEventArgs>();
	private final EventHandler<SQLEventArgs> error = new EventHandler<SQLEventArgs>();
	
	private Connection conn = null;
	
	private IObjectPool<Triplet<String, Object, UUID>> backlog = null;
	private volatile boolean busy = false;
	private volatile boolean connected = false;
	private Timer backlogTimer = null;
	
	private volatile static Method m = null;
	private volatile static URLClassLoader loader = null;
	private static final String SQLITE_JAR = "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.21.0.jar";
	
	private static ExecutorService threadPool = Executors.newFixedThreadPool(20, Executors.defaultThreadFactory());
	
	//constructor
	public SQLite() {
		if (m == null || loader == null) {
			File file = getSQLiteFile();
			try {
				loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
				m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
				m.setAccessible(true);
				
				DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC", true, loader).newInstance());
			} catch (Exception ex) {
				
			}
		}
		
		backlogTimer = new Timer(100, onBacklogTimer);
		backlogTimer.setRepeats(true);
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
		
		if (!FileUtil.pathExists(filePath)) {
			try {
				FileUtil.createFile(filePath);
			} catch (Exception ex) {
				throw new RuntimeException("Could not create database file.", ex);
			}
		} else {
			if (!FileUtil.pathIsFile(filePath)) {
				throw new RuntimeException("filePath is not a valid file.");
			}
		}
		
		try {
			conn = (Connection) m.invoke(null, "jdbc:sqlite:" + filePath, new Properties(), Class.forName("org.sqlite.JDBC", true, loader));
		} catch (Exception ex) {
			throw new RuntimeException("Could not connect to database.", ex);
		}
		
		busy = true;
		backlog = new DynamicObjectPool<Triplet<String, Object, UUID>>();
		connected = true;
		backlogTimer.start();
		connect.invoke(this, EventArgs.EMPTY);
		
		threadPool.execute(new Runnable() {
			public void run() {
				sendNext();
			}
		});
	}
	
	public void disconnect() {
		if (!connected) {
			return;
		}
		
		try {
			threadPool.shutdown();
			threadPool.awaitTermination(3000L, TimeUnit.MILLISECONDS);
			threadPool.shutdownNow();
		} catch (Exception ex) {
			
		}
		
		try {
			conn.close();
		} catch (Exception ex) {
			
		}
		
		backlogTimer.stop();
		conn = null;
		backlog.clear();
		connected = false;
		busy = false;
		disconnect.invoke(this, EventArgs.EMPTY);
	}
	
	public UUID query(String q, Object... queryParams) {
		if (q == null || q.isEmpty()) {
			throw new RuntimeException("q cannot be null or empty.");
		}
		
		UUID u = UUID.randomUUID();
		
		if (!connected) {
			backlog.add(new Triplet<String, Object, UUID>(q, queryParams, u));
		} else {
			if (busy || backlog.size() > 0) {
				backlog.add(new Triplet<String, Object, UUID>(q, queryParams, u));
			} else {
				busy = true;
				threadPool.execute(new Runnable() {
					public void run() {
						queryInternal(q, queryParams, u);
					}
				});
			}
		}
		
		return u;
	}
	public UUID query(String q, Map<String, Object> namedQueryParams) {
		if (q == null || q.isEmpty()) {
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		
		UUID u = UUID.randomUUID();
		
		if (!connected) {
			backlog.add(new Triplet<String, Object, UUID>(q, namedQueryParams, u));
		} else {
			if (busy || backlog.size() > 0) {
				backlog.add(new Triplet<String, Object, UUID>(q, namedQueryParams, u));
			} else {
				busy = true;
				threadPool.execute(new Runnable() {
					public void run() {
						queryInternal(q, namedQueryParams, u);
					}
				});
			}
		}
		
		return u;
	}
	
	public boolean isConnected() {
		return connected;
	}
	public boolean isBusy() {
		return busy;
	}
	public boolean isExternal() {
		return false;
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
		return SQLType.SQLite;
	}
	
	//private
	private void queryInternal(String q, Object[] parameters, UUID u) {
		PreparedStatement command = null;
		
		try {
			command = conn.prepareStatement(q);
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		if (parameters != null && parameters.length > 0) {
			try {
				for (int i = 0; i < parameters.length; i++) {
					command.setObject(i + 1, parameters[i]);
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
		}
		
		execute(command, q, parameters, null, u);
	}
	private void queryInternal(String q, Map<String, Object> parameters, UUID u) {
		NamedParameterStatement command = null;
		
		try {
			command = new NamedParameterStatement(conn, q);
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, null, parameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		if (parameters != null && parameters.size() > 0) {
			try {
				for (Entry<String, Object> kvp : parameters.entrySet()) {
					command.setObject(kvp.getKey(), kvp.getValue());
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, null, parameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
		}
		
		execute(command.getPreparedStatement(), q, null, parameters, u);
	}
	
	private void execute(PreparedStatement command, String q, Object[] parameters, Map<String, Object> namedParameters, UUID u) {
		try {
			command.execute();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		SQLData d = new SQLData();
		try {
			d.recordsAffected = command.getUpdateCount();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		ResultSet results = null;
		try {
			results = command.getResultSet();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNext();
			return;
		}
		
		if (results != null) {
			ResultSetMetaData metaData = null;
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
			ArrayList<String> tColumns = new ArrayList<String>();
			try {
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					tColumns.add(metaData.getColumnName(i));
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
			d.columns = tColumns.toArray(new String[0]);
			
			ArrayList<Object[]> tData = new ArrayList<Object[]>();
			try {
				while (results.next()) {
					Object[] tVals = new Object[tColumns.size()];
					for (int i = 0; i < tColumns.size(); i++) {
						tVals[i] = results.getObject(i + 1);
					}
					tData.add(tVals);
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
			
			try {
				while(command.getMoreResults()) {
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
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNext();
				return;
			}
			
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNext();
		} else {
			d.columns = new String[0];
			d.data = new Object[0][];
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNext();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sendNext() {
		if (backlog.size() == 0) {
			busy = false;
			return;
		}
		
		Triplet<String, Object, UUID> first = backlog.popFirst();
		if (first == null) {
			busy = false;
			return;
		}
		
		if (first.getCenter() instanceof Map) {
			queryInternal(first.getLeft(), (Map<String, Object>) first.getCenter(), first.getRight());
		} else {
			queryInternal(first.getLeft(), (Object[]) first.getCenter(), first.getRight());
		}
	}
	
	private ActionListener onBacklogTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (!busy && backlog.size() > 0) {
				busy = true;
				sendNext();
			}
		}
	};
	
	private static File getSQLiteFile() {
		File file = new File("libs" + FileUtil.DIRECTORY_SEPARATOR_CHAR + "sqlite.jar");
		
		if (FileUtil.pathExists(file) && !FileUtil.pathIsFile(file)) {
			FileUtil.deleteDirectory(file);
		}
		if (!FileUtil.pathExists(file)) {
			URL url = null;
			try {
				File d = new File(file.getParent());
	    		if (d != null) {
	    			d.mkdirs();
	    		}
				
				url = new URL(SQLITE_JAR);
				InputStream in = url.openStream();
				Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				in.close();
			} catch (Exception ex) {
				
			}
		}
		
		return file;
	}
}
