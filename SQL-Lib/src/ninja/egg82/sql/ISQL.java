package ninja.egg82.sql;

import java.util.Map;
import java.util.UUID;

import ninja.egg82.enums.SQLType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.patterns.events.EventArgs;
import ninja.egg82.patterns.events.EventHandler;

public interface ISQL {
	//functions
	void connect(String address, int port, String user, String pass, String dbName);
	void connect(String address, String user, String pass, String dbName);
	void connect(String filePath);
	
	void disconnect();
	
	UUID query(String q, Object... queryParams);
	UUID parallelQuery(String q, Object... queryParams);
	UUID query(String q, Map<String, Object> namedQueryParams);
	UUID parallelQuery(String q, Map<String, Object> namedQueryParams);
	
	boolean isConnected();
	boolean isBusy();
	
	EventHandler<EventArgs> onConnect();
	EventHandler<EventArgs> onDisconnect();
	EventHandler<SQLEventArgs> onData();
	EventHandler<SQLEventArgs> onError();
	
	SQLType getType();
}
