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

package ninja.egg82.registry.nulls;

import java.util.function.BiFunction;
import java.util.function.Function;

import ninja.egg82.registry.interfaces.IRegistry;

/**
 *
 * @author egg82
 */

public class NullRegistry implements IRegistry {
    //vars
    
    //constructor
    public NullRegistry() {
        
    }
    
    //events
    
    //public
    public void initialize() {
        
    }
    
    public void setRegister(String type, Object data) {
        
    }
    public Object getRegister(String type) {
        return null;
    }
    
    public void clear() {
        
    }
    public void reset() {
        
    }
    
    public String[] registryNames() {
        return new String[0];
    }
    public boolean contains(String type) {
    	return false;
    }
    public void computeIfPresent(String type,  BiFunction<? super String, ? super Object, ? extends Object> func) {
    	
    }
    public void computeIfAbsent(String type,  Function<? super String, ? super Object> func) {
    	
    }
    
    //private
    
}
