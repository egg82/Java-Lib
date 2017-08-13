package ninja.egg82.sql;

import java.util.UUID;

import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface ISQL {
	//functions
	void connect(String address, short port, String user, String pass, String dbName);
	void connect(String address, String user, String pass, String dbName);
	void connect(String filePath);
	
	void disconnect();
	
	UUID query(String q, Object... queryParams);
	
	boolean isConnected();
	boolean isBusy();
	boolean isExternal();
	
	EventHandler<EventArgs> onConnect();
	EventHandler<EventArgs> onDisconnect();
	EventHandler<SQLEventArgs> onData();
	EventHandler<SQLEventArgs> onError();
}
