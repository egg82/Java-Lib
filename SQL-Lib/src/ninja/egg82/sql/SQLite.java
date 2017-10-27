package ninja.egg82.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.Timer;

import org.apache.commons.lang.NotImplementedException;

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
	private PreparedStatement command = null;
	
	private IObjectPool<Triplet<String, Object[], UUID>> backlog = null;
	private volatile boolean busy = false;
	private volatile boolean connected = false;
	private Timer backlogTimer = null;
	
	//constructor
	public SQLite() {
		backlogTimer = new Timer(100, onBacklogTimer);
		backlogTimer.setRepeats(true);
	}
	
	//public
	public void connect(String address, String user, String pass, String dbName) {
		throw new NotImplementedException("This database type does not support external (non-file) databases.");
	}
	public void connect(String address, short port, String user, String pass, String dbName) {
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
			conn = DriverManager.getConnection("jdbc:sqlite:" + filePath);
		} catch (Exception ex) {
			throw new RuntimeException("Could not connect to database.", ex);
		}
		
		connected =  false;
		busy = true;
		backlog = new DynamicObjectPool<Triplet<String, Object[], UUID>>();
		connected = true;
		backlogTimer.start();
		connect.invoke(this, EventArgs.EMPTY);
		
		new Thread(new Runnable() {
			public void run() {
				sendNext();
			}
		}).start();
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
		command = null;
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
			backlog.add(new Triplet<String, Object[], UUID>(q, queryParams, u));
		} else {
			if (busy || backlog.size() > 0) {
				backlog.add(new Triplet<String, Object[], UUID>(q, queryParams, u));
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
		try {
			command = conn.prepareStatement(q);
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		if (parameters != null && parameters.length > 0) {
			try {
				for (int i = 0; i < parameters.length; i++) {
					command.setObject(i + 1, parameters[i]);
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
				return;
			}
		}
		
		try {
			command.execute();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		SQLData d = new SQLData();
		try {
			d.recordsAffected = command.getUpdateCount();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		ResultSet results = null;
		try {
			results = command.getResultSet();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			sendNextInternal();
			return;
		}
		
		if (results != null) {
			ResultSetMetaData metaData = null;
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
				return;
			}
			ArrayList<String> tColumns = new ArrayList<String>();
			try {
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					tColumns.add(metaData.getColumnName(i));
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
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
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
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
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				sendNextInternal();
				return;
			}
			
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			data.invoke(this, new SQLEventArgs(q, parameters, new SQLError(), d, u));
			sendNextInternal();
		} else {
			d.columns = new String[0];
			d.data = new Object[0][];
			data.invoke(this, new SQLEventArgs(q, parameters, new SQLError(), d, u));
			sendNextInternal();
		}
	}
	
	private void sendNext() {
		if (backlog.size() == 0) {
			busy = false;
			return;
		}
		
		Triplet<String, Object[], UUID> first = backlog.popFirst();
		queryInternal(first.getLeft(), first.getCenter(), first.getRight());
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
}
