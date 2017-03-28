package ninja.egg82.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public final class FileUtil {
    //vars
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final Charset ASCII = Charset.forName("ASCII");
	
	public static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
	public static final char DIRECTORY_SEPARATOR_CHAR = File.separatorChar;
	
	private static HashMap<String, FileInputStream> inStreams = new HashMap<String, FileInputStream>();
	private static HashMap<String, FileOutputStream> outStreams = new HashMap<String, FileOutputStream>();
    
    //constructor
    public FileUtil() {
        
    }
    
    //public
    public static void createFile(String path) throws Exception {
    	createFile(path, true);
    }
    public static void createFile(String path, boolean createDirectory) throws Exception {
    	File f = new File(path);
    	
    	if (createDirectory) {
    		File d = new File(f.getParent());
    		if (d != null) {
    			d.mkdirs();
    		}
    	}
    	
		f.createNewFile();
    }
    public static void deleteFile(String path) {
    	if (!pathIsFile(path)) {
    		throw new RuntimeException("Path is not a file.");
    	}
    	
    	File f = new File(path);
    	f.delete();
    }
    
    public static void createDirectory(String path) {
    	if (pathExists(path)) {
    		return;
    	}
    	
    	File d = new File(path);
		d.mkdirs();
    }
    public static void deleteDirectory(String path) {
    	if (!pathExists(path)) {
    		return;
    	}
    	if (pathIsFile(path)) {
    		throw new RuntimeException("Path is not a directory");
    	}
    	
    	File d = new File(path);
    	
    	for (File p : d.listFiles()) {
    		if (p.isFile()) {
    			p.delete();
    		} else {
    			deleteDirectory(p.getPath());
    		}
    	}
    	
		d.delete();
    }
    
    public static boolean pathIsFile(String path) {
    	if (!pathExists(path)) {
    		return false;
    	}
    	
    	File p = new File(path);
    	try {
    		return p.isFile();
    	} catch (Exception ex) {
    		return false;
    	}
    }
    public static boolean pathExists(String path) {
    	if (path == null || path.isEmpty()) {
    		return false;
    	}
    	
    	File p = new File(path);
    	try {
    		return p.exists();
    	} catch (Exception ex) {
    		return false;
    	}
    }
    public static boolean fileIsLocked(String path) {
    	if (!pathExists(path) || !pathIsFile(path)) {
    		return false;
    	}
    	
    	File f = new File(path);
    	try {
    		return (f.canRead() && f.canWrite()) ? false : true;
    	} catch (Exception ex) {
    		return false;
    	}
    }
    public static long getTotalBytes(String path) {
    	if (!pathExists(path) || !pathIsFile(path)) {
    		return 0L;
    	}
    	
    	try {
    		return new File(path).length();
    	} catch (Exception ex) {
    		return 0L;
    	}
    }
    
    public synchronized static void open(String path) throws Exception {
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	if (!pathExists(path)) {
    		throw new RuntimeException("Path does not exist.");
    	}
    	if (!pathIsFile(path)) {
    		throw new RuntimeException("Path is not a fale.");
    	}
    	
    	if (inStreams.containsKey(path) || outStreams.containsKey(path)) {
    		close(path);
    	}
    	
    	inStreams.put(path, new FileInputStream(path));
    	outStreams.put(path, new FileOutputStream(path, false));
    }
    public synchronized static void close(String path) throws Exception {
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	if (!inStreams.containsKey(path)) {
    		return;
    	}
    	
    	inStreams.get(path).close();
    	inStreams.remove(path);
    	outStreams.get(path).close();
    	outStreams.remove(path);
    }
    public synchronized static void closeAll() throws Exception {
    	Set<Entry<String, FileInputStream>> inEntries = inStreams.entrySet();
    	Iterator<Entry<String, FileInputStream>> inI = inEntries.iterator();
    	
    	while (inI.hasNext()) {
    		Entry<String, FileInputStream> entry = inI.next();
    		entry.getValue().close();
    	}
    	
    	inStreams.clear();
    	
    	Set<Entry<String, FileOutputStream>> outEntries = outStreams.entrySet();
    	Iterator<Entry<String, FileOutputStream>> outI = outEntries.iterator();
    	
    	while (outI.hasNext()) {
    		Entry<String, FileOutputStream> entry = outI.next();
    		entry.getValue().close();
    	}
    	
    	outStreams.clear();
    }
    
    public synchronized static boolean isOpen(String path) {
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	
    	return (inStreams.containsKey(path)) ? true : false;
    }
    
    public synchronized static byte[] read(String path, long position) throws Exception {
    	return read(path, position, -1L);
    }
    public synchronized static byte[] read(String path, long position, long length) throws Exception {
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	if (!inStreams.containsKey(path)) {
    		throw new RuntimeException("File is not open.");
    	}
    	
    	if (position < 0L) {
    		position = 0L;
    	}
    	long totalBytes = getTotalBytes(path);
    	if (length < 0L || length > totalBytes - position) {
    		length = totalBytes - position;
    	}
    	
    	byte[] buffer =  new byte[(int) Math.min(Integer.MAX_VALUE, length)];
    	
    	FileInputStream stream = inStreams.get(path);
    	stream.getChannel().position(position);
    	stream.read(buffer, 0, (int) length);
    	
    	return buffer;
    }
    public synchronized static void write(String path, byte[] bytes, long position) throws Exception {
    	if (bytes == null || bytes.length == 0) {
    		return;
    	}
    	
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	if (!outStreams.containsKey(path)) {
    		throw new RuntimeException("File is not open.");
    	}
    	
    	if (position < 0L) {
    		position = 0L;
    	}
    	
    	FileOutputStream stream = outStreams.get(path);
    	stream.getChannel().position(position);
    	stream.write(bytes);
    	stream.flush();
    }
    
    public synchronized static void erase(String path) throws Exception {
    	if (path == null) {
    		throw new IllegalArgumentException("path cannot be null.");
    	}
    	
    	if (outStreams.containsKey(path)) {
    		FileOutputStream stream = outStreams.get(path);
        	stream.write(new byte[0]);
        	stream.flush();
    	} else {
    		deleteFile(path);
    		createFile(path);
    	}
    }
    
    //private
    
}
