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
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

public class MySQL implements ISQL {
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
	private static final String MYSQL_JAR = "https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.45.zip";
	
	//constructor
	public MySQL() {
		if (m == null || loader == null) {
			File file = getMySQLFile();
			try {
				loader = new URLClassLoader(new URL[] {file.toURI().toURL()});
				m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
				m.setAccessible(true);
				
				DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver", true, loader).newInstance());
			} catch (Exception ex) {
				
			}
		}
		
		backlogTimer = new Timer(100, onBacklogTimer);
		backlogTimer.setRepeats(true);
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
		
		Properties props = new Properties();
		props.put("user", user);
		props.put("password", pass);
		
		try {
			conn = (Connection) m.invoke(null, "jdbc:mysql://" + address + ":" + port + "/" + dbName, props, Class.forName("com.mysql.jdbc.Driver", true, loader));
		} catch (Exception ex) {
			throw new RuntimeException("Could not connect to database.", ex);
		}
		
		busy = true;
		backlog = new DynamicObjectPool<Triplet<String, Object, UUID>>();
		connected = true;
		backlogTimer.start();
		connect.invoke(this, EventArgs.EMPTY);
		
		new Thread(new Runnable() {
			public void run() {
				sendNext();
			}
		}).start();
	}
	public void connect(String filePath) {
		throw new NotImplementedException("This database type does not support internal (file) databases.");
	}
	public void connect(String filePath, String password) {
		throw new NotImplementedException("This database type does not support internal (file) databases.");
	}
	
	public void disconnect() {
		if (!connected) {
			return;
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
			throw new IllegalArgumentException("q cannot be null or empty.");
		}
		
		UUID u = UUID.randomUUID();
		
		if (!connected) {
			backlog.add(new Triplet<String, Object, UUID>(q, queryParams, u));
		} else {
			if (busy || backlog.size() > 0) {
				backlog.add(new Triplet<String, Object, UUID>(q, queryParams, u));
			} else {
				busy = true;
				new Thread(new Runnable() {
					public void run() {
						queryInternal(q, queryParams, u);
					}
				}).start();
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
				new Thread(new Runnable() {
					public void run() {
						queryInternal(q, namedQueryParams, u);
					}
				}).start();
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
	private void queryInternal(String q, Object[] parameters, UUID u) {
		PreparedStatement command = null;
		
		try {
			command = conn.prepareStatement(q);
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		if (parameters != null && parameters.length > 0) {
			try {
				for (int i = 0; i < parameters.length; i++) {
					command.setObject(i + 1, parameters[i]);
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, null, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
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
			sendNextInternal();
			return;
		}
		
		if (parameters != null && parameters.size() > 0) {
			try {
				for (Entry<String, Object> kvp : parameters.entrySet()) {
					command.setObject(kvp.getKey(), kvp.getValue());
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, null, parameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
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
			sendNextInternal();
			return;
		}
		
		SQLData d = new SQLData();
		try {
			d.recordsAffected = command.getUpdateCount();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		ResultSet results = null;
		try {
			results = command.getResultSet();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		if (results != null) {
			ResultSetMetaData metaData = null;
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
				return;
			}
			ArrayList<String> tColumns = new ArrayList<String>();
			try {
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					tColumns.add(metaData.getColumnName(i));
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
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
				sendNextInternal();
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
				sendNextInternal();
				return;
			}
			
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNextInternal();
		} else {
			d.columns = new String[0];
			d.data = new Object[0][];
			data.invoke(this, new SQLEventArgs(q, parameters, namedParameters, new SQLError(), d, u));
			sendNextInternal();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sendNext() {
		if (backlog.size() == 0) {
			busy = false;
			return;
		}
		
		Triplet<String, Object, UUID> first = backlog.popFirst();
		if (first.getCenter() instanceof Map) {
			queryInternal(first.getLeft(), (Map<String, Object>) first.getCenter(), first.getRight());
		} else {
			queryInternal(first.getLeft(), (Object[]) first.getCenter(), first.getRight());
		}
	}
	private void sendNextInternal() {
		new Thread(new Runnable() {
			public void run() {
				sendNext();
			}
		}).start();
	}
	
	private ActionListener onBacklogTimer = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (!busy && backlog.size() > 0) {
				busy = true;
				sendNext();
			}
		}
	};
	
	private static File getMySQLFile() {
		File file = new File("libs" + FileUtil.DIRECTORY_SEPARATOR_CHAR + "mysql.jar");
		
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
				
				url = new URL(MYSQL_JAR);
				ZipInputStream zip = new ZipInputStream(url.openStream());
				ZipEntry entry = null;
				
				do {
					entry = zip.getNextEntry();
					if (entry.getName().equals("mysql-connector-java-5.1.45\\mysql-connector-java-5.1.45-bin.jar") || entry.getName().equals("mysql-connector-java-5.1.45/mysql-connector-java-5.1.45-bin.jar")) {
						break;
					}
				} while (entry != null);
				
				if (entry == null) {
					zip.close();
					return file;
				}
				
				OutputStream out = new FileOutputStream(file);
				
				byte[] buffer = new byte[1024];
				int len = zip.read(buffer);
				while (len != -1) {
					out.write(buffer, 0, len);
					len = zip.read(buffer);
				};
				
				out.close();
				zip.close();
			} catch (Exception ex) {
				
			}
		}
		
		return file;
	}
}
