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

package ninja.egg82.patterns.pool;

import java.util.ArrayList;

import ninja.egg82.enums.ServiceType;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.prototype.interfaces.IPrototype;
import ninja.egg82.patterns.prototype.interfaces.IPrototypeFactory;
import ninja.egg82.utils.Util;

/**
 *
 * @author egg82
 */

public class DynamicObjectPool {
    //vars
    private IPrototypeFactory prototypeFactory = (IPrototypeFactory) ServiceLocator.getService(ServiceType.PROTOTYPE_FACTORY);
    private String prototypeName = null;
    
    private ArrayList<IPrototype> _usedPool = new ArrayList<IPrototype>();
    private ArrayList<IPrototype> _freePool = new ArrayList<IPrototype>();
    
    //constructor
    public DynamicObjectPool(String prototypeName, IPrototype prototype) {
        if (prototypeName == null || prototypeName.isEmpty()) {
            throw new Error("prototypeName cannot be null");
        }
        if (prototype == null) {
            throw new Error("prototype cannot be null");
        }
        
        this.prototypeName = prototypeName;
        prototypeFactory.addPrototype(prototypeName, prototype);
    }
    
    //events
    
    //public
    public void initialize() {
        initialize(1);
    }
    public void initialize(int numObjects) {
        if (numObjects < 0) {
            return;
        }
        
        for (int i = 0; i < numObjects; i++) {
            _freePool.add(prototypeFactory.createInstance(prototypeName));
        }
    }
    
    public IPrototype getObject() {
        IPrototype obj = (_freePool.size() == 0) ? prototypeFactory.createInstance(prototypeName) : _freePool.remove(0);
        _usedPool.add(obj);
        return obj;
    }
    public void returnObj(IPrototype obj) {
        for (IPrototype obj2 : _usedPool) {
            if (obj == obj2) {
                _usedPool.remove(obj);
                _freePool.add(obj);
                return;
            }
        }
    }
    
    public int usedPoolLength() {
        return _usedPool.size();
    }
    public int freePoolLength() {
        return _freePool.size();
    }
    public int size() {
        return _usedPool.size() + _freePool.size();
    }
    
    public void clear() {
        for (IPrototype obj : _usedPool) {
            Util.invokeMethod("destroy", obj);
            Util.invokeMethod("dispose", obj);
        }
        
        _usedPool.clear();
        gc();
    }
    public void gc() {
        for (IPrototype obj : _freePool) {
            Util.invokeMethod("destroy", obj);
            Util.invokeMethod("dispose", obj);
        }
        
        _freePool.clear();
        System.gc();
    }
    
    public void resize(int to) {
        resize(to, false);
    }
    public void resize(int to, Boolean hard) {
        if (to < 0) {
            return;
        }
        
        if (to == _usedPool.size() + _freePool.size()) {
            return;
        } else if (to > _usedPool.size() + _freePool.size()) {
            for (int i = _usedPool.size() + _freePool.size(); i < to; i++) {
                _freePool.add(prototypeFactory.createInstance(prototypeName));
            }
            return;
        } else if (to == 0 && hard) {
            clear();
            return;
        }
        
        for (int i = _freePool.size() - 1; i >= 0; i--) {
            _freePool.remove(i);
            if (_usedPool.size() + _freePool.size() == to) {
                System.gc();
                return;
            }
        }
        
        if (!hard) {
            return;
        }
        
        for (int i = _usedPool.size() - 1; i >= 0; i--) {
            _usedPool.remove(i);
            if (_usedPool.size() == to) {
                System.gc();
                return;
            }
        }
    }
    
    //private
    
}
