package ninja.egg82.events;

import java.util.UUID;

import ninja.egg82.core.SQLData;
import ninja.egg82.core.SQLError;
import ninja.egg82.patterns.events.EventArgs;

public class SQLEventArgs extends EventArgs {
	//vars
	public static final SQLEventArgs EMPTY = new SQLEventArgs(null, null, new SQLError(), new SQLData(), null);
	
	private String query = null;
	private Object[] queryParameters = null;
	private SQLError error = null;
	private SQLData data = null;
	private UUID uuid = null;
	
	//constructor
	public SQLEventArgs(String query, Object[] queryParameters, SQLError error, SQLData data, UUID uuid) {
		super();
		
		this.query = query;
		this.queryParameters = queryParameters;
		this.error = error;
		this.data = data;
		this.uuid = uuid;
	}
	
	//public
	public String getQuery() {
		return query;
	}
	public Object[] getQueryParameters() {
		return queryParameters;
	}
	public SQLError getSQLError() {
		return error;
	}
	public SQLData getData() {
		return data;
	}
	public UUID getUuid() {
		return uuid;
	}
	
	//private
	
}
