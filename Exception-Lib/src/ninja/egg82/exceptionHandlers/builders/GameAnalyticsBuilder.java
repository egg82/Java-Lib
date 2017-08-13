package ninja.egg82.exceptionHandlers.builders;

public class GameAnalyticsBuilder implements IBuilder {
	//vars
	private String gameKey = null;
	private String secretKey = null;
	private String version = null;
	private String userId = null;
	
	//constructor
	public GameAnalyticsBuilder(String gameKey, String secretKey, String version, String userId) {
		this.gameKey = gameKey;
		this.secretKey = secretKey;
		this.version = version;
		this.userId = userId;
	}
	
	//public
	public String[] getParams() {
		return new String[] {
			gameKey,
			secretKey,
			version,
			userId
		};
	}
	
	//private
	
}
