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

package com.egg82.patterns.prototype;

import com.egg82.patterns.prototype.interfaces.IPrototype;
import com.egg82.patterns.prototype.interfaces.IPrototypeFactory;
import java.util.HashMap;

/**
 *
 * @author egg82
 */

public class PrototypeFactory implements IPrototypeFactory {
    //vars
    private HashMap<String, IPrototype> instances = new HashMap<String, IPrototype>();
    
    //constructor
    public PrototypeFactory() {
        
    }
    
    //events
    
    //public
    public void addPrototype(String name, IPrototype prototype) {
        if (prototype == null) {
            throw new Error("prototype cannot be null");
        }
        
        instances.put(name, prototype);
    }
    public IPrototype createInstance(String name) {
        return (instances.containsKey(name)) ? (instances.get(name).clone()) : null;
    }
    
    //private
    
}