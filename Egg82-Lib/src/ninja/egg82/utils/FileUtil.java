package ninja.egg82.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Map.Entry;

import ninja.egg82.exceptions.ArgumentNullException;

import java.util.concurrent.ConcurrentHashMap;

public final class FileUtil {
    //vars
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	public static final Charset ASCII = Charset.forName("ASCII");
	
	public static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
	public static final char DIRECTORY_SEPARATOR_CHAR = File.separatorChar;
	public static final String LINE_SEPARATOR = System.lineSeparator();
	
	private static ConcurrentHashMap<String, FileInputStream> inStreams = new ConcurrentHashMap<String, FileInputStream>();
	private static ConcurrentHashMap<String, FileOutputStream> outStreams = new ConcurrentHashMap<String, FileOutputStream>();
    
    //constructor
    public FileUtil() {
        
    }
    
    //public
    public static void createFile(File file) throws Exception {
    	createFile(file, true);
    }
    public static void createFile(String path) throws Exception {
    	createFile(path, true);
    }
    
    public static void createFile(File file, boolean createDirectory) throws Exception {
    	if (createDirectory) {
    		File d = new File(file.getParent());
    		d.mkdirs();
    	}
    	
		file.createNewFile();
    }
    public static void createFile(String path, boolean createDirectory) throws Exception {
    	File f = new File(path);
    	
    	if (createDirectory) {
    		File d = new File(f.getParent());
    		d.mkdirs();
    	}
    	
		f.createNewFile();
    }
    
    public static void deleteFile(File file) {
    	if (!pathIsFile(file)) {
    		throw new RuntimeException("Path is not a file.");
    	}
    	
    	file.delete();
    }
    public static void deleteFile(String path) {
    	if (!pathIsFile(path)) {
    		throw new RuntimeException("Path is not a file.");
    	}
    	
    	File f = new File(path);
    	f.delete();
    }
    
    public static void createDirectory(File file) {
    	if (pathExists(file)) {
    		return;
    	}
    	
		file.mkdirs();
    }
    public static void createDirectory(String path) {
    	if (pathExists(path)) {
    		return;
    	}
    	
    	File d = new File(path);
		d.mkdirs();
    }
    
    public static void deleteDirectory(File file) {
    	if (!pathExists(file)) {
    		return;
    	}
    	if (pathIsFile(file)) {
    		throw new RuntimeException("Path is not a directory.");
    	}
    	
    	File[] list = file.listFiles();
    	if (list == null) {
    		file.delete();
    		return;
    	}
    	
    	for (File p : list) {
    		if (p.isFile()) {
    			p.delete();
    		} else {
    			deleteDirectory(p.getPath());
    		}
    	}
    	
		file.delete();
    }
    public static void deleteDirectory(String path) {
    	if (!pathExists(path)) {
    		return;
    	}
    	if (pathIsFile(path)) {
    		throw new RuntimeException("Path is not a directory.");
    	}
    	
    	File d = new File(path);
    	
    	File[] list = d.listFiles();
    	if (list == null) {
    		d.delete();
    		return;
    	}
    	
    	for (File p : list) {
    		if (p.isFile()) {
    			p.delete();
    		} else {
    			deleteDirectory(p.getPath());
    		}
    	}
    	
		d.delete();
    }
    
    public static boolean pathIsFile(File file) {
    	if (!pathExists(file)) {
    		return false;
    	}
    	
    	try {
    		return file.isFile();
    	} catch (Exception ex) {
    		return false;
    	}
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
    
    public static boolean pathExists(File file) {
    	if (file == null) {
    		return false;
    	}
    	
    	try {
    		return file.exists();
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
    
    public static boolean fileIsLocked(File file) {
    	if (!pathExists(file) || !pathIsFile(file)) {
    		return false;
    	}
    	
    	try {
    		return (file.canRead() && file.canWrite()) ? false : true;
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
    
    public static long getTotalBytes(File file) {
    	if (!pathExists(file) || !pathIsFile(file)) {
    		return 0L;
    	}
    	
    	String path = file.getAbsolutePath();
    	if (inStreams.containsKey(path)) {
    		try {
	    		long oldPosition = inStreams.get(path).getChannel().position();
	    		inStreams.get(path).getChannel().position(0L);
	    		long available = inStreams.get(path).available();
	    		inStreams.get(path).getChannel().position(oldPosition);
	    		return available;
    		} catch (Exception ex) {
	    		return 0L;
	    	}
    	}
    	
    	try {
    		return file.length();
    	} catch (Exception ex) {
    		return 0L;
    	}
    }
    public static long getTotalBytes(String path) {
    	if (!pathExists(path) || !pathIsFile(path)) {
    		return 0L;
    	}
    	
    	if (inStreams.containsKey(path)) {
    		try {
	    		long oldPosition = inStreams.get(path).getChannel().position();
	    		inStreams.get(path).getChannel().position(0L);
	    		long available = inStreams.get(path).available();
	    		inStreams.get(path).getChannel().position(oldPosition);
	    		return available;
    		} catch (Exception ex) {
	    		return 0L;
	    	}
    	}
    	
    	try {
    		return new File(path).length();
    	} catch (Exception ex) {
    		return 0L;
    	}
    }
    
    public static void open(String path) throws Exception {
    	if (path == null) {
    		throw new ArgumentNullException("path");
    	}
    	
    	open(new File(path));
    }
    @SuppressWarnings("resource")
	public static void open(File file) throws Exception {
    	if (file == null) {
    		throw new ArgumentNullException("file");
    	}
    	
    	String path = file.getAbsolutePath();
    	
    	if (!pathExists(file)) {
    		throw new RuntimeException("Path does not exist.");
    	}
    	if (!pathIsFile(file)) {
    		throw new RuntimeException("Path is not a file.");
    	}
    	
    	if (inStreams.containsKey(path) || outStreams.containsKey(path)) {
    		return;
    	}
    	
    	inStreams.putIfAbsent(path, new FileInputStream(path));
    	byte[] old = read(file, 0);
    	outStreams.putIfAbsent(path, new FileOutputStream(path, false));
    	write(file, old, 0);
    }
    public static void close(String path) throws Exception {
    	if (path == null) {
    		throw new ArgumentNullException("path");
    	}
    	
    	close(new File(path));
    }
    public static void close(File file) throws Exception {
    	if (file == null) {
    		throw new ArgumentNullException("file");
    	}
    	
    	String path = file.getAbsolutePath();
    	
    	if (!inStreams.containsKey(path) && !outStreams.containsKey(path)) {
    		return;
    	}
    	
    	FileInputStream inStream = inStreams.get(path);
    	if (inStream != null) {
    		inStream.close();
    		inStreams.remove(path);
    	}
    	FileOutputStream outStream = outStreams.get(path);
    	if (outStream != null) {
	    	outStream.close();
	    	outStreams.remove(path);
    	}
    }
    public static void closeAll() throws Exception {
    	for (Entry<String, FileInputStream> kvp : inStreams.entrySet()) {
    		kvp.getValue().close();
    	}
    	inStreams.clear();
    	
    	for (Entry<String, FileOutputStream> kvp : outStreams.entrySet()) {
    		kvp.getValue().close();
    	}
    	outStreams.clear();
    }
    
    public static boolean isOpen(String path) {
    	return (path != null) ? isOpen(new File(path)) : false;
    }
    public static boolean isOpen(File file) {
    	return (file != null && inStreams.containsKey(file.getAbsolutePath())) ? true : false;
    }
    
    public static byte[] read(String path, long position) throws Exception {
    	return read(path, position, -1L);
    }
    public static byte[] read(File file, long position) throws Exception {
    	return read(file, position, -1L);
    }
    public static byte[] read(String path, long position, long length) throws Exception {
    	if (path == null) {
    		throw new ArgumentNullException("path");
    	}
    	
    	return read(new File(path), position, length);
    }
    @SuppressWarnings("resource")
	public static byte[] read(File file, long position, long length) throws Exception {
    	if (file == null) {
    		throw new ArgumentNullException("file");
    	}
    	
    	String path = file.getAbsolutePath();
    	
    	FileInputStream stream = inStreams.get(path);
    	if (stream == null) {
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
    	
    	stream.getChannel().position(position);
    	stream.read(buffer, 0, (int) length);
    	
    	return buffer;
    }
    public static void write(String path, byte[] bytes, long position) throws Exception {
    	if (path == null) {
    		throw new ArgumentNullException("path");
    	}
    	
    	write(new File(path), bytes, position);
    }
    @SuppressWarnings("resource")
	public static void write(File file, byte[] bytes, long position) throws Exception {
    	if (file == null) {
    		throw new ArgumentNullException("file");
    	}
    	if (bytes == null || bytes.length == 0) {
    		return;
    	}
    	
    	FileOutputStream stream = outStreams.get(file.getAbsolutePath());
    	if (stream == null) {
    		throw new RuntimeException("File is not open.");
    	}
    	
    	if (position < 0L) {
    		position = 0L;
    	}
    	
    	stream.getChannel().position(position);
    	stream.write(bytes);
    	stream.flush();
    }
    
    public static void erase(String path) throws Exception {
    	if (path == null) {
    		throw new ArgumentNullException("path");
    	}
    	
    	erase(new File(path));
    }
    @SuppressWarnings("resource")
	public static void erase(File file) throws Exception {
    	if (file == null) {
    		throw new ArgumentNullException("file");
    	}
    	
    	String path = file.getAbsolutePath();
    	
    	FileOutputStream stream = outStreams.get(path);
    	if (stream != null) {
        	stream.write(new byte[0]);
        	stream.flush();
    	} else {
    		deleteFile(path);
    		createFile(path);
    	}
    }
    
    //private
    
}
