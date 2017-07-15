package ninja.egg82.exceptions;

public class ArgumentNullException extends IllegalArgumentException {
	//vars
	private static final long serialVersionUID = 7414032404379335085L;
	
	private String paramName = null;
	
	//constructor
	public ArgumentNullException() {
		super("A provided argument cannot be null.");
	}
	public ArgumentNullException(Exception innerException) {
		super("A provided argument cannot be null.", innerException);
	}
	public ArgumentNullException(String paramName) {
		super(paramName + " cannot be null.");
		this.paramName = paramName;
	}
	public ArgumentNullException(String paramName, Exception innerException) {
		super(paramName + " cannot be null.", innerException);
		this.paramName = paramName;
	}
	
	//public
	public String getParamName() {
		return paramName;
	}
	
	//private
	
}
