/*
 * Copyright (c) egg82 (Alexander Mason) 2016
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/

package com.egg82.registry;

import com.egg82.events.registry.RegistryEvent;
import com.egg82.patterns.Observer;
import com.egg82.registry.interfaces.IRegistry;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author egg82
 */

public class Registry implements IRegistry {
    //vars
    public static final ArrayList<Observer> OBSERVERS = new ArrayList<Observer>();
    
    protected Boolean initialized = false;
    protected HashMap<String, Object> registry = new HashMap<String, Object>();
    
    //constructor
    public Registry() {
        
    }
    
    //events
    
    //public
    public void initialize() {
        initialized = true;
    }
    
    public void setRegister(String type, Object data) {
        String event = (registry.containsKey(type)) ? ((data != null) ? RegistryEvent.VALUE_CHANGED : RegistryEvent.VALUE_REMOVED) : RegistryEvent.VALUE_ADDED;
        
        if (data == null) {
        	registry.remove(type);
        	dispatch(event, ImmutableMap.of("name", type, "value", new Object()));
        } else {
        	registry.put(type, data);
        	dispatch(event, ImmutableMap.of("name", type, "value", data));
        }
    }
    public Object getRegister(String type) {
        return registry.get(type);
    }
    
    public void clear() {
        registry.clear();
    }
    public void reset() {
        
    }
    
    public String[] registryNames() {
        Set<String> keys = registry.keySet();
        return keys.toArray(new String[keys.size()]);
    }
    public boolean contains(String type) {
    	return registry.containsKey(type);
    }
    
    //private
    protected void dispatch(String event, Object data) {
        Observer.dispatch(OBSERVERS, this, event, data);
    }
}
