package ninja.egg82.utils;

import java.nio.charset.Charset;

import org.json.JSONObject;

import ninja.egg82.patterns.registries.IVariableRegistry;

public final class SettingsUtil {
    //vars
    
    //constructor
    public SettingsUtil() {
        
    }
    
    //public
    public static void load(String path, IVariableRegistry<String> registry) throws Exception {
    	load(path, registry, FileUtil.UTF_8);
    }
    public static void load(String path, IVariableRegistry<String> registry, Charset enc) throws Exception {
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
        
        setRegistry(json, registry);
    }
    
    public static void save(String path, IVariableRegistry<String> registry) throws Exception {
    	save(path, registry, FileUtil.UTF_8);
    }
    public static void save(String path, IVariableRegistry<String> registry, Charset enc) throws Exception {
    	if (FileUtil.pathExists(path)) {
    		if (!FileUtil.pathIsFile(path)) {
    			throw new RuntimeException("path is not a file.");
    		}
    	} else {
    		FileUtil.createFile(path);
    	}
    	
        String[] names = registry.getKeys();
        
        boolean fileWasOpen = true;
        
        if (!FileUtil.isOpen(path)) {
        	FileUtil.open(path);
        	fileWasOpen = false;
        }
        
        JSONObject json = new JSONObject();
        
        for (String name : names) {
            json.put(name, registry.getRegister(name));
        }
        
        FileUtil.erase(path);
        FileUtil.write(path, json.toString(4).getBytes(enc), 0);
        
        if (!fileWasOpen) {
        	FileUtil.close(path);
        }
    }
    
    public static void loadSave(String path, IVariableRegistry<String> registry) throws Exception {
        loadSave(path, registry, FileUtil.UTF_8);
    }
    public static void loadSave(String path, IVariableRegistry<String> registry, Charset enc) throws Exception {
    	if (!FileUtil.pathExists(path)) {
    		FileUtil.createFile(path);
    	}
    	
    	load(path, registry, enc);
        save(path, registry, enc);
    }
    
    //private
    private static void setRegistry(JSONObject json, IVariableRegistry<String> registry) {
    	for (String i : json.keySet()) {
    		Object obj = json.get(i);
    		
    		if (obj.getClass().isArray()) {
    			if (registry.hasRegister(i)) {
    				try {
    					registry.setRegister(i, registry.getRegisterClass(i).cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	} else {
            		try {
            			registry.setRegister(i, Object[].class.cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	}
    		} else {
    			if (registry.hasRegister(i)) {
    				try {
    					registry.setRegister(i, registry.getRegisterClass(i).cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	} else {
            		try {
            			registry.setRegister(i, Object.class.cast(obj));
    				} catch (Exception ex) {
    					
    				}
            	}
    		}
        }
    }
}
