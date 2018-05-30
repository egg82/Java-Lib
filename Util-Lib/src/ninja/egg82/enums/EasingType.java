package ninja.egg82.enums;

public enum EasingType {
	LINEAR,
	SINE_IN,
	SINE_OUT,
	SINE_IN_OUT,
	QUINTIC_IN,
	QUINTIC_OUT,
	QUINTIC_IN_OUT,
	QUARTIC_IN,
	QUARTIC_OUT,
	QUARTIC_IN_OUT,
	EXPONENTIAL_IN,
	EXPONENTIAL_OUT,
	EXPONENTIAL_IN_OUT,
	ELASTIC_IN,
	ELASTIC_OUT,
	ELASTIC_IN_OUT,
	CIRCULAR_IN,
	CIRCULAR_OUT,
	CIRCULAR_IN_OUT,
	BACK_IN,
	BACK_OUT,
	BACK_IN_OUT,
	BOUNCE_IN,
	BOUNCE_OUT,
	BOUNCE_IN_OUT,
	CUBIC_IN,
	CUBIC_OUT,
	CUBIC_IN_OUT;
	
	public static EasingType matchType(final String name) {
		if (name == null) {
			throw new IllegalArgumentException("name cannot be null.");
		}
		
		String filtered = name.toUpperCase(java.util.Locale.ENGLISH);
		filtered = filtered.replaceAll("\\s+", "_").replaceAll("\\W", "");
		
		return valueOf(filtered);
	}
}
