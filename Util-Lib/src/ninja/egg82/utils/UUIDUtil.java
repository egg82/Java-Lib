package ninja.egg82.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtil {
	//vars
	
	//constructor
	public UUIDUtil() {
		
	}
	
	//public
	public static UUID readUuid(InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("stream cannot be null.");
		}
		
		byte[] bytes = new byte[16];
		int read = 0;
		try {
			read = stream.read(bytes);
		} catch (Exception ex) {
			return null;
		}
		if (read < bytes.length) {
			return null;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long high = buffer.getLong();
		long low = buffer.getLong();
		
		return new UUID(high, low);
	}
	public static UUID toUuid(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("bytes cannot be null.");
		}
		
		if (bytes.length < 16) {
			return null;
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		long high = buffer.getLong();
		long low = buffer.getLong();
		
		return new UUID(high, low);
	}
	public static byte[] toBytes(UUID uuid) {
		if (uuid == null) {
			throw new IllegalArgumentException("uuid cannot be null.");
		}
		
		long high = uuid.getMostSignificantBits();
		long low = uuid.getLeastSignificantBits();
		
		ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(high);
		buffer.putLong(low);
		
		return buffer.array();
	}
	public static void writeUuid(UUID uuid, OutputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("stream cannot be null.");
		}
		
		try {
			stream.write(toBytes(uuid));
		} catch (Exception ex) {
			
		}
	}
	
	public static int numBytes() {
		return 16;
	}
	
	//private
	
}
