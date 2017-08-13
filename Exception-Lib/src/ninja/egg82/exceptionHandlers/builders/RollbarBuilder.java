package ninja.egg82.exceptionHandlers.builders;

public class RollbarBuilder implements IBuilder {
	//vars
	private String accessToken = null;
	private String environment = null;
	private String version = null;
	private String userId = null;
	
	//constructor
	public RollbarBuilder(String accessToken, String environment, String version, String userId) {
		this.accessToken = accessToken;
		this.environment = environment;
		this.version = version;
		this.userId = userId;
	}
	
	//public
	public String[] getParams() {
		return new String[] {
			accessToken,
			environment,
			version,
			userId
		};
	}
	
	//private
	
}
