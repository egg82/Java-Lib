package ninja.egg82.core;

import java.util.Map;
import java.util.UUID;

public class SQLQueueData {
	//vars
	private String query = null;
	private Map<String, Object> namedParams = null;
	private Object[] unnamedParams = null;
	private UUID uuid = null;
	private boolean parallel = false;
	
	//constructor
	public SQLQueueData(UUID uuid, String query, Map<String, Object> namedParams, boolean parallel) {
		this(uuid, query, namedParams, null, parallel);
	}
	public SQLQueueData(UUID uuid, String query, Object[] unnamedParams, boolean parallel) {
		this(uuid, query, null, unnamedParams, parallel);
	}
	public SQLQueueData(UUID uuid, String query, Map<String, Object> namedParams, Object[] unnamedParams, boolean parallel) {
		this.uuid = uuid;
		this.query = query;
		this.namedParams = namedParams;
		this.unnamedParams = unnamedParams;
		this.parallel = parallel;
	}
	
	//public
	public String getQuery() {
		return query;
	}
	public Map<String, Object> getNamedParams() {
		return namedParams;
	}
	public Object[] getUnnamedParams() {
		return unnamedParams;
	}
	public UUID getUuid() {
		return uuid;
	}
	public boolean getParallel() {
		return parallel;
	}
	
	//private
	
}
