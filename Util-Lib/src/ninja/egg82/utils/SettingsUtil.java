package ninja.egg82.utils;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public final class SettingsUtil {
    //vars
    
    //constructor
    public SettingsUtil() {
        
    }
    
    //public
    public static void load(String path, Map<String, Object> map) throws Exception {
    	load(path, map, FileUtil.UTF_8);
    }
    public static void load(String path, Map<String, Object> map, Charset enc) throws Exception {
    	if (!FileUtil.pathExists(path)) {
    		throw new RuntimeException("path does not exist.");
    	}
    	if (!FileUtil.pathIsFile(path)) {
    		throw new RuntimeException("path is not a file.");
    	}
    	
    	boolean fileWasOpen = true;
    	
    	if (!FileUtil.isOpen(path)) {
    		FileUtil.open(path);
    		fileWasOpen = false;
    	}
    	
    	int totalBytes = ((FileUtil.getTotalBytes(path) > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) FileUtil.getTotalBytes(path));
        String str = new String(FileUtil.read(path, 0L, totalBytes), enc).replaceAll("\r", "").replaceAll("\n", "");
        
        if (!fileWasOpen) {
        	FileUtil.close(path);
        }
        
        if (str.isEmpty()) {
        	return;
        }
        
        JSONObject json = null;
        
        try {
            json = new JSONObject(str);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot create JSON object.", ex);
        }
        
        setMap(json, map);
    }
    
    public static void save(String path, Map<String, Object> map) throws Exception {
    	save(path, map, FileUtil.UTF_8);
    }
    public static void save(String path, Map<String, Object> map, Charset enc) throws Exception {
    	if (FileUtil.pathExists(path)) {
    		if (!FileUtil.pathIsFile(path)) {
    			throw new RuntimeException("path is not a file.");
    		}
    	} else {
    		FileUtil.createFile(path);
    	}
    	
        Set<String> names = map.keySet();
        
        boolean fileWasOpen = true;
        
        if (!FileUtil.isOpen(path)) {
        	FileUtil.open(path);
        	fileWasOpen = false;
        }
        
        JSONObject json = new JSONObject();
        
        for (String name : names) {
            json.put(name, map.get(name));
        }
        
        FileUtil.erase(path);
        FileUtil.write(path, json.toString(4).getBytes(enc), 0);
        
        if (!fileWasOpen) {
        	FileUtil.close(path);
        }
    }
    
    public static void loadSave(String path, Map<String, Object> map) throws Exception {
        loadSave(path, map, FileUtil.UTF_8);
    }
    public static void loadSave(String path, Map<String, Object> map, Charset enc) throws Exception {
    	if (!FileUtil.pathExists(path)) {
    		FileUtil.createFile(path);
    	}
    	
    	load(path, map, enc);
        save(path, map, enc);
    }
    
    //private
    private static void setMap(JSONObject json, Map<String, Object> map) {
    	for (String i : json.keySet()) {
    		Object obj = json.get(i);
    		
    		if (obj.getClass().isArray()) {
    			if (map.containsKey(i)) {
    				try {
    					map.put(i, map.get(i).getClass().cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	} else {
            		try {
            			map.put(i, Object[].class.cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	}
    		} else {
    			if (map.containsKey(i)) {
    				try {
    					map.put(i, map.get(i).getClass().cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	} else {
            		try {
            			map.put(i, Object.class.cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	}
    		}
        }
    }
}
