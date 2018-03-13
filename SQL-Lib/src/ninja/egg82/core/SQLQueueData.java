package ninja.egg82.core;

import java.util.Map;
import java.util.UUID;

public class SQLQueueData {
	//vars
	private volatile String query = null;
	private volatile Map<String, Object> namedParams = null;
	private volatile Object[] unnamedParams = null;
	private volatile UUID uuid = null;
	
	//constructor
	public SQLQueueData() {
		
	}
	
	//public
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	public Map<String, Object> getNamedParams() {
		return namedParams;
	}
	public void setNamedParams(Map<String, Object> namedParams) {
		this.namedParams = namedParams;
	}
	
	public Object[] getUnnamedParams() {
		return unnamedParams;
	}
	public void setUnnamedParams(Object[] unnamedParams) {
		this.unnamedParams = unnamedParams;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	
	public void clear() {
		this.query = null;
		this.namedParams = null;
		this.unnamedParams = null;
		this.uuid = null;
	}
	
	//private
	
}
