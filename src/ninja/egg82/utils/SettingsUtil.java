package ninja.egg82.utils;

import java.nio.charset.Charset;

import org.json.JSONObject;

import ninja.egg82.patterns.IRegistry;

public class SettingsUtil {
    //vars
    
    //constructor
    public SettingsUtil() {
        
    }
    
    //public
    public static void load(String path, IRegistry registry) throws Exception {
    	load(path, registry, FileUtil.UTF_8);
    }
    public static void load(String path, IRegistry registry, Charset enc) throws Exception {
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
        String str = new String(FileUtil.read(path, 0L, (long) totalBytes), enc).replaceAll("\r", "").replaceAll("\n", "");
        
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
            
        }
        
        if (json != null) {
            setRegistry(json, registry);
        }
    }
    
    public static void save(String path, IRegistry registry) throws Exception {
    	save(path, registry, FileUtil.UTF_8);
    }
    public static void save(String path, IRegistry registry, Charset enc) throws Exception {
    	if (FileUtil.pathExists(path)) {
    		if (!FileUtil.pathIsFile(path)) {
    			throw new RuntimeException("path is not a file.");
    		}
    	} else {
    		FileUtil.createFile(path);
    	}
    	
        String[] names = registry.getRegistryNames();
        
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
    
    public static void loadSave(String path, IRegistry registry) throws Exception {
        loadSave(path, registry, FileUtil.UTF_8);
    }
    public static void loadSave(String path, IRegistry registry, Charset enc) throws Exception {
    	if (!FileUtil.pathExists(path)) {
    		FileUtil.createFile(path);
    	}
    	
    	load(path, registry, enc);
        save(path, registry, enc);
    }
    
    //private
    private static void setRegistry(JSONObject json, IRegistry registry) {
    	for (String i : json.keySet()) {
    		Object obj = json.get(i);
    		
    		if (obj.getClass().isArray()) {
    			if (registry.hasRegister(i)) {
            		registry.setRegister(i, registry.getRegisterClass(i), obj);
            	} else {
            		registry.setRegister(i, Object[].class, obj);
            	}
    		} else {
    			if (registry.hasRegister(i)) {
            		registry.setRegister(i, registry.getRegisterClass(i), obj);
            	} else {
            		registry.setRegister(i, Object.class, obj);
            	}
    		}
    		
            /*if (obj.getClass().isArray()) {
                Object[] arr = (Object[]) registry.getRegister(i);
                deepCopy(obj, arr);
                registry.setRegister(i, arr);
            } else if (obj instanceof JSONObject) {
                Object obj = registry.getRegister(i);
                deepCopy(obj, obj);
                registry.setRegister(i, obj);
            } else {
            	registry.setRegister(i, obj);
            }*/
        }
    	
        /*for (String i : from.keySet()) {
            if (to.getRegister(i) != null) {
                if (from.get(i).getClass().isArray()) {
                    Object[] arr = (Object[]) to.getRegister(i);
                    deepCopy(from.get(i), arr);
                    to.setRegister(i, arr);
                } else if (from.get(i) instanceof JSONObject) {
                    Object obj = to.getRegister(i);
                    deepCopy(from.get(i), obj);
                    to.setRegister(i, obj);
                } else {
                    to.setRegister(i, from.get(i));
                }
            }
        }*/
    }
    /*private void deepCopy(Object from, Object to) {
        for (Method m : from.getClass().getMethods()) {
            String i = m.getName();
            if (Util.getMethod(i, to) != null) {
                Object obj = null;
                Object obj2 = null;
                
                try {
                    obj = Util.getMethod(i, from).get(from);
                    obj2 = Util.getMethod(i, to).get(to);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    continue;
                }
                
                if (obj.getClass().isArray() || obj instanceof JSONObject) {
                    deepCopy(obj, obj2);
                } else {
                    try {
                        Util.getMethod(i, to).set(to, obj);
                    } catch (Exception ex) {
                         System.out.println(ex.getMessage());
                    }
                }
            }
        }
    }*/
}
