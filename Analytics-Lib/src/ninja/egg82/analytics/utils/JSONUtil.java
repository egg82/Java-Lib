package ninja.egg82.analytics.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentDeque;

public class JSONUtil {
	//vars
	private static IConcurrentDeque<JSONParser> pool = new DynamicConcurrentDeque<JSONParser>(); // JSONParser is not stateless and thus requires a pool in multi-threaded environments
	
	//constructor
	public JSONUtil() {
		pool.add(new JSONParser());
	}
	
	//public
	public static JSONObject parseObject(String input) throws ParseException, ClassCastException {
		return (JSONObject) parseGeneric(input);
	}
	public static JSONArray parseArray(String input) throws ParseException, ClassCastException {
		return (JSONArray) parseGeneric(input);
	}
	public static Object parseGeneric(String input) throws ParseException {
		if (input == null) {
			return null;
		}
		
		JSONParser parser = getParser();
		Object retVal = parser.parse(input);
		pool.add(parser);
		return retVal;
	}
	
	//private
	private static JSONParser getParser() {
		JSONParser parser = pool.pollFirst();
		if (parser == null) {
			parser = new JSONParser();
		}
		return parser;
	}
}
