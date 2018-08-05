package ninja.egg82.analytics;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.zip.GZIPOutputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ninja.egg82.analytics.core.LogLevel;
import ninja.egg82.analytics.utils.JSONUtil;
import ninja.egg82.concurrent.DynamicConcurrentDeque;
import ninja.egg82.concurrent.DynamicConcurrentSet;
import ninja.egg82.concurrent.IConcurrentDeque;
import ninja.egg82.concurrent.IConcurrentSet;
import ninja.egg82.crypto.CryptoHelper;
import ninja.egg82.crypto.ICryptoHelper;
import ninja.egg82.utils.ThreadUtil;

public class GameAnalyticsHandler implements AutoCloseable {
	//vars
	private String endpoint = "https://api.gameanalytics.com/v2";
	private String gameKey = null;
	private String secretKey = null;
	private String systemName = null;
	
	private String systemManufacturer = null;
	private String version = null;
	private String userId = null;
	private String sessionId = UUID.randomUUID().toString();
	private int sessionNum = -1;
	
	private AtomicInteger transactionNum = new AtomicInteger(0);
	
	private long startTime = -1L;
	
	private ICryptoHelper cryptoHelper = new CryptoHelper();
	
	private AtomicBoolean ready = new AtomicBoolean(false);
	private long offset = -1L;
	
	private ScheduledExecutorService threadPool = null;
	
	private IConcurrentDeque<JSONObject> queuedEvents = new DynamicConcurrentDeque<JSONObject>();
	private IConcurrentDeque<JSONObject> failedEvents = new DynamicConcurrentDeque<JSONObject>();
	
	private IConcurrentSet<String> allExceptions = new DynamicConcurrentSet<String>();
	
	//constructor
	public GameAnalyticsHandler(String gameKey, String secretKey, String version, String userId, int sessionNum, String threadName) {
		if (gameKey == null) {
			throw new IllegalArgumentException("gameKey cannot be null.");
		}
		if (secretKey == null) {
			throw new IllegalArgumentException("secretKey cannot be null.");
		}
		if (version == null) {
			throw new IllegalArgumentException("version cannot be null.");
		}
		if (userId == null) {
			throw new IllegalArgumentException("userId cannot be null.");
		}
		if (sessionNum <= 0) {
			throw new IllegalArgumentException("sessionNum cannot be <= 0.");
		}
		if (threadName == null) {
			throw new IllegalArgumentException("threadName cannot be null.");
		}
		
		startTime = System.currentTimeMillis();
		
		this.gameKey = gameKey;
		this.secretKey = secretKey;
		this.version = version;
		this.userId = userId;
		this.sessionNum = sessionNum;
		
		String lowerName = System.getProperty("os.name").toLowerCase();
		if (lowerName.contains("win")) {
			systemName = "windows";
			systemManufacturer = "microsoft";
		} else if (lowerName.contains("mac")) {
			systemName = "macintosh";
			systemManufacturer = "apple";
		} else if (lowerName.contains("nix") || lowerName.contains("nux") || lowerName.contains("aix")) {
			systemName = "unix";
			systemManufacturer = "bell";
		} else if (lowerName.contains("sunos")) {
			systemName = "solaris";
			systemManufacturer = "sun";
		} else {
			systemName = "unknown: '" + System.getProperty("os.name") + "'";
			systemManufacturer = "unknown";
		}
		
		threadPool = ThreadUtil.createScheduledPool(0, Runtime.getRuntime().availableProcessors(), 120L * 1000L, new ThreadFactoryBuilder().setNameFormat(threadName + "-GameAnalytics-%d").build());
		threadPool.scheduleAtFixedRate(onBacklogThread, 30L * 1000L, 30L * 1000L, TimeUnit.MILLISECONDS);
		
		threadPool.submit(new Runnable() {
			public void run() {
				initialize();
			}
		});
	}
	
	//public
	@SuppressWarnings("unchecked")
	public void close() {
		if (!ready.getAndSet(false)) {
			return;
		}
		
		try {
			threadPool.shutdown();
			if (!threadPool.awaitTermination(15L * 1000L, TimeUnit.MILLISECONDS)) {
				threadPool.shutdownNow();
			}
		} catch (Exception ex) {
			
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "session_end");
		event.put("length", Long.valueOf(Math.floorDiv(System.currentTimeMillis() - startTime, 1000L)));
		populateDefaults(event);
		
		sendEvents(event);
	}
	
	public void sendRealCurrencyPurchase(String purchasedItemCategory, String purchasedItemName, double amountInUSD) {
		sendRealCurrencyPurchase(purchasedItemCategory, purchasedItemName, null, amountInUSD, "USD");
	}
	public void sendRealCurrencyPurchase(String purchasedItemCategory, String purchasedItemName, double amount, String currencyCode) {
		sendRealCurrencyPurchase(purchasedItemCategory, purchasedItemName, null, amount, currencyCode);
	}
	public void sendRealCurrencyPurchase(String purchasedItemCategory, String purchasedItemName, String menuName, double amountInUSD) {
		sendRealCurrencyPurchase(purchasedItemCategory, purchasedItemName, menuName, amountInUSD, "USD");
	}
	@SuppressWarnings("unchecked")
	public void sendRealCurrencyPurchase(String purchasedItemCategory, String purchasedItemName, String menuName, double amount, String currencyCode) {
		if (purchasedItemCategory == null) {
			throw new IllegalArgumentException("purchasedItemCategory cannot be null.");
		}
		if (purchasedItemName == null) {
			throw new IllegalArgumentException("purchasedItemName cannot be null.");
		}
		if (currencyCode == null) {
			throw new IllegalArgumentException("currencyCode cannot be null.");
		}
		
		if (amount < 0.0d) {
			amount = amount * -1.0d;
		}
		
		int transactionNum = this.transactionNum.incrementAndGet();
		
		JSONObject event = new JSONObject();
		event.put("category", "business");
		event.put("event_id", purchasedItemCategory + ":" + purchasedItemName);
		event.put("amount", Integer.valueOf((int) Math.floor(amount * 100.0d)));
		event.put("currency", currencyCode.toUpperCase());
		event.put("transaction_num", Integer.valueOf(transactionNum));
		if (menuName != null && !menuName.isEmpty()) {
			event.put("cart_type", menuName);
		}
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendVirtualCurrencyPurchase(String currencyName, double amount, String purchasedItemCategory, String purchasedItemName) {
		if (currencyName == null) {
			throw new IllegalArgumentException("currencyName cannot be null.");
		}
		if (purchasedItemCategory == null) {
			throw new IllegalArgumentException("purchasedItemCategory cannot be null.");
		}
		if (purchasedItemName == null) {
			throw new IllegalArgumentException("purchasedItemName cannot be null.");
		}
		
		if (amount > 0.0d) {
			amount = amount * -1.0d;
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "resource");
		event.put("event_id", "Sink:" + currencyName + ":" + purchasedItemCategory + ":" + purchasedItemName);
		event.put("amount", Float.valueOf((float) amount));
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	@SuppressWarnings("unchecked")
	public void sendVirtualCurrencyEarned(String currencyName, double amount, String earningSourceCategory, String earningSourceName) {
		if (currencyName == null) {
			throw new IllegalArgumentException("currencyName cannot be null.");
		}
		if (earningSourceCategory == null) {
			throw new IllegalArgumentException("earningSourceCategory cannot be null.");
		}
		if (earningSourceName == null) {
			throw new IllegalArgumentException("earningSourceName cannot be null.");
		}
		
		if (amount < 0.0d) {
			amount = amount * -1.0d;
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "resource");
		event.put("event_id", "Source:" + currencyName + ":" + earningSourceCategory + ":" + earningSourceName);
		event.put("amount", Float.valueOf((float) amount));
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	public void sendProgressionStart(String levelName) {
		sendProgressionStart(null, null, levelName);
	}
	public void sendProgressionStart(String worldName, String levelName) {
		sendProgressionStart(worldName, null, levelName);
	}
	@SuppressWarnings("unchecked")
	public void sendProgressionStart(String worldName, String subworldName, String levelName) {
		if (levelName == null) {
			throw new IllegalArgumentException("levelName cannot be null.");
		}
		if (subworldName != null && worldName == null) {
			throw new IllegalArgumentException("worldName cannot be null when subworldName is set. Please switch the non-null value to worldName instead of subworldName.");
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "progression");
		if (worldName != null && subworldName != null) {
			event.put("event_id", "Start:" + worldName + ":" + subworldName + ":" + levelName);
		} else if (worldName != null) {
			event.put("event_id", "Start:" + worldName + ":" + levelName);
		} else {
			event.put("event_id", "Start:" + levelName);
		}
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	public void sendProgressionFail(String levelName, long score) {
		sendProgressionFail(null, null, levelName, score, -1);
	}
	public void sendProgressionFail(String levelName, long score, int attemptNum) {
		sendProgressionFail(null, null, levelName, score, attemptNum);
	}
	public void sendProgressionFail(String worldName, String levelName, long score) {
		sendProgressionFail(worldName, null, levelName, score, -1);
	}
	public void sendProgressionFail(String worldName, String levelName, long score, int attemptNum) {
		sendProgressionFail(worldName, null, levelName, score, attemptNum);
	}
	public void sendProgressionFail(String worldName, String subworldName, String levelName, long score) {
		sendProgressionFail(worldName, subworldName, levelName, score, -1);
	}
	@SuppressWarnings("unchecked")
	public void sendProgressionFail(String worldName, String subworldName, String levelName, long score, int attemptNum) {
		if (levelName == null) {
			throw new IllegalArgumentException("levelName cannot be null.");
		}
		if (subworldName != null && worldName == null) {
			throw new IllegalArgumentException("worldName cannot be null when subworldName is set. Please switch the non-null value to worldName instead of subworldName.");
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "progression");
		if (worldName != null && subworldName != null) {
			event.put("event_id", "Fail:" + worldName + ":" + subworldName + ":" + levelName);
		} else if (worldName != null) {
			event.put("event_id", "Fail:" + worldName + ":" + levelName);
		} else {
			event.put("event_id", "Fail:" + levelName);
		}
		event.put("score", Long.valueOf(score));
		if (attemptNum > 0) {
			event.put("attempt_num", Integer.valueOf(attemptNum));
		}
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	public void sendProgressionComplete(String levelName, long score) {
		sendProgressionFail(null, null, levelName, score, -1);
	}
	public void sendProgressionComplete(String levelName, long score, int attemptNum) {
		sendProgressionFail(null, null, levelName, score, attemptNum);
	}
	public void sendProgressionComplete(String worldName, String levelName, long score) {
		sendProgressionFail(worldName, null, levelName, score, -1);
	}
	public void sendProgressionComplete(String worldName, String levelName, long score, int attemptNum) {
		sendProgressionFail(worldName, null, levelName, score, attemptNum);
	}
	public void sendProgressionComplete(String worldName, String subworldName, String levelName, long score) {
		sendProgressionFail(worldName, subworldName, levelName, score, -1);
	}
	@SuppressWarnings("unchecked")
	public void sendProgressionComplete(String worldName, String subworldName, String levelName, long score, int attemptNum) {
		if (levelName == null) {
			throw new IllegalArgumentException("levelName cannot be null.");
		}
		if (subworldName != null && worldName == null) {
			throw new IllegalArgumentException("worldName cannot be null when subworldName is set. Please switch the non-null value to worldName instead of subworldName.");
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "progression");
		if (worldName != null && subworldName != null) {
			event.put("event_id", "Complete:" + worldName + ":" + subworldName + ":" + levelName);
		} else if (worldName != null) {
			event.put("event_id", "Complete:" + worldName + ":" + levelName);
		} else {
			event.put("event_id", "Complete:" + levelName);
		}
		event.put("score", Long.valueOf(score));
		if (attemptNum > 0) {
			event.put("attempt_num", Integer.valueOf(attemptNum));
		}
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendCustomMetric(String field1, String field2, String field3, String field4, String field5) {
		StringBuilder builder = new StringBuilder();
		if (field1 != null) {
			builder.append(field1);
			builder.append(':');
		}
		if (field2 != null) {
			builder.append(field2);
			builder.append(':');
		}
		if (field3 != null) {
			builder.append(field3);
			builder.append(':');
		}
		if (field4 != null) {
			builder.append(field4);
			builder.append(':');
		}
		if (field5 != null) {
			builder.append(field5);
			builder.append(':');
		}
		
		String id = builder.toString();
		if (id.length() > 0) {
			id = id.substring(0, id.length() - 1);
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "design");
		event.put("event_id", id);
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	@SuppressWarnings("unchecked")
	public void sendCustomMetric(String field1, String field2, String field3, String field4, String field5, double value) {
		StringBuilder builder = new StringBuilder();
		if (field1 != null) {
			builder.append(field1);
			builder.append(':');
		}
		if (field2 != null) {
			builder.append(field2);
			builder.append(':');
		}
		if (field3 != null) {
			builder.append(field3);
			builder.append(':');
		}
		if (field4 != null) {
			builder.append(field4);
			builder.append(':');
		}
		if (field5 != null) {
			builder.append(field5);
			builder.append(':');
		}
		
		String id = builder.toString();
		if (id.length() > 0) {
			id = id.substring(0, id.length() - 1);
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "design");
		event.put("event_id", id);
		event.put("value", Float.valueOf((float) value));
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendException(Throwable ex) {
		if (ex == null) {
			throw new IllegalArgumentException("ex cannot be null.");
		}
		
		String exString = null;
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			ex.printStackTrace(pw);
			exString = sw.toString();
		} catch (Exception ex2) {
			ex2.printStackTrace();
		}
		
		if (!allExceptions.add(exString)) {
			return;
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "error");
		event.put("severity", (ex instanceof Error) ? "critical" : "error");
		event.put("message", (exString != null) ? exString : ex.getMessage());
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	public void sendRecord(LogRecord record) {
		if (record == null) {
			throw new IllegalArgumentException("record cannot be null.");
		}
		
		if (record.getThrown() != null) {
			sendException(record.getThrown());
		} else if (record.getMessage() != null) {
			if (record.getLevel() == Level.WARNING) {
				sendMessage(record.getMessage(), LogLevel.WARNING);
			} else if (record.getLevel() == Level.INFO) {
				sendMessage(record.getMessage(), LogLevel.INFO);
			} else if (record.getLevel() == Level.FINE || record.getLevel() == Level.FINER || record.getLevel() == Level.FINEST) {
				sendMessage(record.getMessage(), LogLevel.DEBUG);
			} else {
				sendMessage(record.getMessage(), LogLevel.ERROR);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendMessage(String message, LogLevel level) {
		if (message == null) {
			throw new IllegalArgumentException("message cannot be null.");
		}
		
		if (!allExceptions.add(message)) {
			return;
		}
		
		JSONObject event = new JSONObject();
		event.put("category", "error");
		event.put("severity", level.name().toLowerCase());
		event.put("message", message);
		populateDefaults(event);
		
		queuedEvents.add(event);
		if (ready.get()) {
			threadPool.submit(onSendThread);
		}
	}
	
	public ImmutableList<JSONObject> getUnsentEvents() {
		List<JSONObject> total = new ArrayList<JSONObject>();
		
		for (JSONObject event : failedEvents) {
			total.add(event);
		}
		for (JSONObject event : queuedEvents) {
			total.add(event);
		}
		
		while (total.remove(null)) {
			// Just iteratively removing null values
		}
		
		return ImmutableList.copyOf(total);
	}
	
	//private
	private Runnable onSendThread = new Runnable() {
		public void run() {
			sendNext();
		}
	};
	private Runnable onBacklogThread = new Runnable() {
		public void run() {
			if (!ready.get() || queuedEvents.isEmpty()) {
				return;
			}
			
			sendNext();
		}
	};
	
	@SuppressWarnings("unchecked")
	private void sendNext() {
		if (!ready.get()) {
			return;
		}
		
		List<JSONObject> events = new ArrayList<JSONObject>();
		JSONObject first = null;
		long size = cryptoHelper.toBytes(new JSONArray().toJSONString()).length;
		
		// TODO This code is for GZIP-compressed output which isn't working in GA yet
		/*do {
			first = queuedEvents.pollFirst();
			
			if (first != null) {
				events.add(first);
				
				size += cryptoHelper.toBytes(first.toJSONString()).length;
			}
		} while (first != null && size < 6873786L); // 1MB, when gzipped (approx)
		
		if (events.size() == 0) {
			return;
		}
		
		// Ensuring the gzipped size fits constraints
		do {
			JSONArray d = new JSONArray();
			for (JSONObject object : events) {
				d.add(object);
			}
			
			try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); GZIPOutputStream out = new GZIPOutputStream(stream)) {
				out.write(cryptoHelper.toBytes(d.toJSONString()));
				out.flush();
				size = stream.size();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (size >= 1000000L) {
				queuedEvents.addFirst(events.remove(events.size() - 1));
			}
		} while (size >= 1000000L);*/
		
		// TODO this is the non-GZIP version on the above code
		do {
			first = queuedEvents.pollFirst();
			
			if (first != null) {
				events.add(first);
				
				size += cryptoHelper.toBytes(first.toJSONString()).length;
			}
		} while (first != null && size < 1000000L);
		
		if (events.size() == 0) {
			return;
		}
		
		do {
			JSONArray d = new JSONArray();
			for (JSONObject object : events) {
				d.add(object);
			}
			
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				out.write(cryptoHelper.toBytes(d.toJSONString()));
				out.flush();
				size = out.size();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (size >= 1000000L) {
				queuedEvents.addFirst(events.remove(events.size() - 1));
			}
		} while (size >= 1000000L);
		
		// Send data
		boolean good = false;
		int tries = 0;
		do {
			Object returned = sendEvents(events.toArray(new JSONObject[0]));
			
			if (returned == null) {
				good = false;
			} else if (returned instanceof JSONArray) {
				JSONArray r = (JSONArray) returned;
				for (Object object : r) {
					JSONObject o = (JSONObject) object;
					if (o.containsKey("errors") && o.containsKey("event")) {
						failedEvents.add((JSONObject) o.get("event"));
					}
				}
				good = true;
			} else {
				good = true;
			}
			
			tries++;
			if (!good) {
				try {
					Thread.sleep(10L * 1000L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} while (!good && tries <= 3);
		
		if (!good) {
			failedEvents.addAll(events);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initialize() {
		JSONObject data = new JSONObject();
		
		data.put("platform", systemName);
		data.put("os_version", System.getProperty("os.name").toLowerCase());
		data.put("sdk_version", "rest api v2");
		
		JSONObject init = null;
		boolean enabled = false;
		do {
			init = (JSONObject) sendData("/" + gameKey + "/init", cryptoHelper.toBytes(data.toJSONString()));
			if (init == null) {
				try {
					Thread.sleep(10L * 1000L);
				} catch (Exception ex) {
					
				}
			} else {
				enabled = ((Boolean) init.get("enabled")).booleanValue();
				if (!enabled) {
					try {
						Thread.sleep(10L * 1000L);
					} catch (Exception ex) {
						
					}
				}
			}
		} while (init == null || !enabled);
		
		long serverTimestamp = ((Number) init.get("server_ts")).longValue();
		offset = Math.floorDiv(System.currentTimeMillis(), 1000L) - serverTimestamp;
		
		JSONObject event = new JSONObject();
		event.put("category", "user");
		populateDefaults(event);
		
		boolean good = false;
		int tries = 0;
		do {
			Object returned = sendEvents(event);
			
			if (returned == null) {
				good = false;
			} else if (returned instanceof JSONArray) {
				JSONArray r = (JSONArray) returned;
				for (Object object : r) {
					JSONObject o = (JSONObject) object;
					if (o.containsKey("errors") && o.containsKey("event")) {
						failedEvents.add((JSONObject) o.get("event"));
					}
				}
				good = true;
			} else {
				good = true;
			}
			
			tries++;
			if (!good) {
				try {
					Thread.sleep(10L * 1000L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} while (!good && tries <= 3);
		
		if (!good) {
			failedEvents.add(event);
		}
		
		ready.set(true);
	}
	
	@SuppressWarnings("unchecked")
	private Object sendEvents(JSONObject... data) {
		if (data == null || data.length == 0) {
			return null;
		}
		
		JSONArray d = new JSONArray();
		for (JSONObject object : data) {
			d.add(object);
		}
		
		return sendData("/" + gameKey + "/events", cryptoHelper.toBytes(d.toJSONString()));
	}
	
	@SuppressWarnings("unchecked")
	private void populateDefaults(JSONObject event) {
		// Required
		event.put("device", System.getProperty("os.name").replaceAll("\\s", ""));
		event.put("v", Integer.valueOf(2));
		event.put("user_id", userId);
		event.put("client_ts", Long.valueOf(Math.floorDiv(System.currentTimeMillis(), 1000L) - offset));
		event.put("sdk_version", "rest api v2");
		event.put("os_version", System.getProperty("os.name").toLowerCase());
		event.put("manufacturer", systemManufacturer);
		event.put("platform", systemName);
		event.put("session_id", sessionId);
		event.put("session_num", Integer.valueOf(sessionNum));
		
		// Optional
		event.put("build", version);
	}
	
	private Object sendData(String urlPart, byte[] postData) {
		HttpURLConnection conn = null;
		
		try {
			conn = (HttpURLConnection) new URL(endpoint + urlPart).openConnection();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		// TODO commented-out GZIP functionality because GA doesn't like it for whatever reason
		
		try {
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Connection", "close");
			conn.setRequestProperty("User-Agent", "egg82/GameAnalytics");
			//conn.setRequestProperty("Content-Encoding", "gzip");
			
			conn.setRequestMethod("POST");
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		/*byte[] compressedBytes = null;
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream(); GZIPOutputStream out = new GZIPOutputStream(stream)) {
			out.write(postData);
			out.flush();
			compressedBytes = stream.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}*/
		
		//byte[] hmac = cryptoHelper.hmac256(compressedBytes, cryptoHelper.toBytes(secretKey));
		byte[] hmac = cryptoHelper.hmac256(postData, cryptoHelper.toBytes(secretKey));
		
		try {
			conn.setRequestProperty("Authorization", cryptoHelper.toString(cryptoHelper.base64Encode(hmac)));
			//conn.setRequestProperty("Content-Length", Integer.toString(compressedBytes.length));
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		try (OutputStream stream = conn.getOutputStream()) {
			//stream.write(compressedBytes);
			stream.write(postData);
			stream.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		try {
			int code = conn.getResponseCode();
			
			if (code == 401) {
				new RuntimeException("Received 401 (Unauthorized) from GameAnalytics. Please ensure the secret key is correct.").printStackTrace();
				return null;
			} else if (code == 413) {
				new RuntimeException("Received 413 (Request Entity Too Large) from GameAnalytics.").printStackTrace();
				return null;
			}
			
			try (InputStream in = (code == 200) ? conn.getInputStream() : conn.getErrorStream(); InputStreamReader reader = new InputStreamReader(in); BufferedReader buffer = new BufferedReader(reader)) {
				StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = buffer.readLine()) != null) {
					builder.append(line);
				}
				
				String error = builder.toString();
				
				if (code == 400) {
					new RuntimeException("Received 400 (Bad Request) from GameAnalytics.", new Exception(error)).printStackTrace();
					if (error.startsWith("[") || error.startsWith("{")) {
						return JSONUtil.parseGeneric(error);
					}
					return null;
				}
				
				if (error.startsWith("[") || error.startsWith("{")) {
					return JSONUtil.parseGeneric(error);
				}
				return null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
