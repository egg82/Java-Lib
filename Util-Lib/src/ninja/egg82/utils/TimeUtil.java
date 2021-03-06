package ninja.egg82.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
	//vars
	private static Pattern timePattern = Pattern.compile("^([0-9]+)\\s?(seconds?|secs?|s|minutes?|mins?|m|hours?|hrs?|h|days?|dys?|d|weeks?|wks?|months?|mos?|years?|yrs?)", Pattern.CASE_INSENSITIVE);
	
	//constructor
	public TimeUtil() {
		
	}
	
	//public
	public static long getTime(String from) {
		if (from == null) {
			throw new IllegalArgumentException("from cannot be null.");
		}
		
		Matcher m = timePattern.matcher(from);
		if (!m.matches()) {
			throw new RuntimeException("\"" + from + "\" does not match expected time pattern.");
		}
		
		long time = 1000L;
		
		if (m.group(2).equalsIgnoreCase("s") || m.group(2).equalsIgnoreCase("sec") || m.group(2).equalsIgnoreCase("secs") || m.group(2).equalsIgnoreCase("second") || m.group(2).equalsIgnoreCase("seconds")) {
			// Do nothing, time is already in seconds
		} else if (m.group(2).equalsIgnoreCase("m") || m.group(2).equalsIgnoreCase("min") || m.group(2).equalsIgnoreCase("mins") || m.group(2).equalsIgnoreCase("minute") || m.group(2).equalsIgnoreCase("minutess")) {
			time *= 60L;
		} else if (m.group(2).equalsIgnoreCase("h") || m.group(2).equalsIgnoreCase("hr") || m.group(2).equalsIgnoreCase("hrs") || m.group(2).equalsIgnoreCase("hour") || m.group(2).equalsIgnoreCase("hours")) {
			time *= 60L * 60L;
		} else if (m.group(2).equalsIgnoreCase("d") || m.group(2).equalsIgnoreCase("dy") || m.group(2).equalsIgnoreCase("dys") || m.group(2).equalsIgnoreCase("day") || m.group(2).equalsIgnoreCase("days")) {
			time *= 60L * 60L * 24L;
		} else if (m.group(2).equalsIgnoreCase("wk") || m.group(2).equalsIgnoreCase("wks") || m.group(2).equalsIgnoreCase("week") || m.group(2).equalsIgnoreCase("weeks")) {
			time *= 60L * 60L * 24L * 7L;
		} else if (m.group(2).equalsIgnoreCase("mo") || m.group(2).equalsIgnoreCase("mos") || m.group(2).equalsIgnoreCase("month") || m.group(2).equalsIgnoreCase("months")) {
			time *= 60L * 60L * 24L * 30L;
		} else if (m.group(2).equalsIgnoreCase("yr") || m.group(2).equalsIgnoreCase("yrs") || m.group(2).equalsIgnoreCase("year") || m.group(2).equalsIgnoreCase("years")) {
			time *= 60L * 60L * 24L * 365L;
		}
		
		time *= Long.parseUnsignedLong(m.group(1));
		
		return time;
	}
	
	public static String timeToHoursMinsSecs(long time) {
		short hours = 0;
		short minutes = 0;
		short seconds = 0;
		
		while (time >= 3600000L) {
			hours++;
			time -= 3600000L;
		}
		while (time >= 60000L) {
			minutes++;
			time -= 60000L;
		}
		while (time >= 1000L) {
			seconds++;
			time -= 1000L;
		}
		if (time >= 700L) {
			seconds++;
		}
		
		while (seconds >= 60) {
			seconds -= 60;
			minutes++;
		}
		while (minutes >= 60) {
			minutes -= 60;
			hours++;
		}
		
		return String.format("%02d", Short.valueOf(hours)) + ":" + String.format("%02d", Short.valueOf(minutes)) + ":" + String.format("%02d", Short.valueOf(seconds));
	}
	public static String timeToYearsMonthsDaysHoursMinsSecs(long time) {
		short years = 0;
		short months = 0;
		short days = 0;
		
		while (time >= 31104000000L) {
			years++;
			time -= 31104000000L;
		}
		while (time >= 2592000000L) {
			months++;
			time -= 2592000000L;
		}
		while (time >= 86400000L) {
			days++;
			time -= 86400000L;
		}
		
		return String.valueOf(years) + ((years == 1) ? " year, " : " years, ") + String.valueOf(months) + ((months == 1) ? " month, " : " months, ") + String.valueOf(days) + ((days == 1) ? " day," : " days,") + " and " + timeToHoursMinsSecs(time);
	}
	public static String timeToDateString(long time) {
		return new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a").format(new Date(time));
	}
	
	//private
	
}
