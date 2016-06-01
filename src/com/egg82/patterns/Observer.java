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

package com.egg82.patterns;

import java.util.ArrayList;

/**
 *
 * @author egg82
 */

public class Observer {
    //vars
    private ArrayList<TriFunction<Object, String, Object, Void>> listeners = new ArrayList<TriFunction<Object, String, Object, Void>>();
    
    //constructor
    public Observer() {
        
    }
    
    //events
    
    //public
    public static void add(ArrayList<Observer> list, Observer observer) {
        if (list == null || observer == null) {
            return;
        }
        
        if(list.contains(observer)) {
            return;
        }
        
        list.add(observer);
    }
    public static void remove(ArrayList<Observer> list, Observer observer) {
        if (list == null || observer == null) {
            return;
        }
        
        list.remove(observer);
    }
    
    public static void dispatch(ArrayList<Observer> list, Object sender, String event) {
        dispatch(list, sender, event, null);
    }
    public static void dispatch(ArrayList<Observer> list, Object sender, String event, Object data) {
        if (list == null || list.isEmpty()) {
            return;
        }
        
        for (Observer observer : list) {
            observer.dispatch(sender, event, data);
        }
    }
    
    public void add(TriFunction<Object, String, Object, Void> listener) {
        if (listener == null) {
            return;
        }
        
        if (listeners.contains(listener)) {
            return;
        }
        
        listeners.add(listener);
    }
    public void remove(TriFunction<Object, String, Object, Void> listener) {
        if (listener == null) {
            return;
        }
        
        listeners.remove(listener);
    }
    public void removeAll() {
        listeners.clear();
    }
    
    public void dispatch(Object sender, String event, Object data) {
        for (TriFunction<Object, String, Object, Void> func : listeners) {
            try {
                func.apply(sender, event, data);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public int numListeners() {
        return listeners.size();
    }
    
    //private
    
}
