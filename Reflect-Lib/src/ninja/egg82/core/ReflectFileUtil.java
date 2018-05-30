package ninja.egg82.core;

import java.io.File;

public class ReflectFileUtil {
	//vars
	
	//constructor
	public ReflectFileUtil() {
		
	}
	
	//public
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
    			deleteDirectory(p);
    		}
    	}
    	
		file.delete();
    }
	
	//private
	
}
