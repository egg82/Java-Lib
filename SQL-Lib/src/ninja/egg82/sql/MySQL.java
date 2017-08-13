package ninja.egg82.sql;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.Timer;

import org.apache.commons.lang.NotImplementedException;

import ninja.egg82.core.SQLData;
import ninja.egg82.core.SQLError;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.tuples.Triplet;

public class MySQL implements ISQL {
	//vars
	private final EventHandler<EventArgs> connect = new EventHandler<EventArgs>();
	private final EventHandler<EventArgs> disconnect = new EventHandler<EventArgs>();
	private final EventHandler<SQLEventArgs> data = new EventHandler<SQLEventArgs>();
	private final EventHandler<SQLEventArgs> error = new EventHandler<SQLEventArgs>();
	
	private Connection conn = null;
	private PreparedStatement command = null;
	
	private ArrayDeque<Triplet<String, Object[], UUID>> backlog = null;
	private boolean busy = false;
	private boolean connected = false;
	private Timer backlogTimer = null;
	
	//constructor
	public MySQL() {
		backlogTimer = new Timer(100, onBacklogTimer);
		backlogTimer.setRepeats(true);
	}
	
	//public
	public void connect(String address, String user, String pass, String dbName) {
		connect(address, (short) 3306, user, pass, dbName);
	}
	public void connect(String address, short port, String user, String pass, String dbName) {
		if (address == null || address.isEmpty()) {
			throw new IllegalArgumentException("address cannot be null or empty.");
		}
		if (port <= 0) {
			throw new IllegalArgumentException("port cannot be <= 0.");
		}
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql:" + address + ":" + port + "/" + dbName, user, pass);
		} catch (Exception ex) {
			throw new RuntimeException("Could not connect to database.", ex);
		}
		
		connected =  false;
		busy = true;
		backlog = new ArrayDeque<Triplet<String, Object[], UUID>>();
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
		command = null;
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
	
	//private
	private void queryInternal(String q, Object[] parameters, UUID u) {
		try {
			command = conn.prepareStatement(q);
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			return;
		}
		
		if (parameters != null && parameters.length > 0) {
			try {
				for (int i = 0; i < parameters.length; i++) {
					command.setObject(i, parameters[i]);
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				return;
			}
		}
		
		try {
			command.execute();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			return;
		}
		
		SQLData d = new SQLData();
		try {
			d.recordsAffected = command.getUpdateCount();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			return;
		}
		
		ResultSet results = null;
		try {
			results = command.getResultSet();
		} catch (Exception ex) {
			error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
			return;
		}
		
		if (results != null) {
			ResultSetMetaData metaData = null;
			try {
				metaData = results.getMetaData();
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				return;
			}
			ArrayList<String> tColumns = new ArrayList<String>();
			try {
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					tColumns.add(metaData.getColumnName(i));
				}
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				return;
			}
			d.columns = tColumns.toArray(new String[0]);
			
			ArrayList<Object[]> tData = new ArrayList<Object[]>();
			
			try {
				do {
					results = command.getResultSet();
					results.beforeFirst();
					while (results.next()) {
						Object[] tVals = new Object[tColumns.size()];
						for (int i = 0; i < tColumns.size(); i++) {
							tVals[i] = results.getObject(i);
						}
						tData.add(tVals);
					}
				} while (command.getMoreResults());
			} catch (Exception ex) {
				error.invoke(this, new SQLEventArgs(q, parameters, new SQLError(ex), new SQLData(), u));
				return;
			}
			
			d.data = new Object[tData.size()][tColumns.size()];
			for (int i = 0; i < tData.size(); i++) {
				for (int j = 0; j < tColumns.size(); j++) {
					d.data[i][j] = tData.get(i)[j];
				}
			}
			
			data.invoke(this, new SQLEventArgs(q, parameters, new SQLError(), d, u));
			new Thread(new Runnable() {
				public void run() {
					sendNext();
				}
			}).start();
		}
	}
	
	private void sendNext() {
		if (backlog.size() == 0) {
			busy = false;
			return;
		}
		
		Triplet<String, Object[], UUID> first = backlog.pop();
		queryInternal(first.getLeft(), first.getCenter(), first.getRight());
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
