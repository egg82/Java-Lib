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

package ninja.egg82.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 *
 * @author egg82
 */

public class Util {
    //vars
    
    //constructor
    public Util() {
        
    }
    
    //events
    
    //public
    public static void invokeMethod(String method, Object obj) {
        Method find = null;
        
        try {
            find = obj.getClass().getMethod(method, (Class<?>[]) null);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        if (find != null) {
            try {
                find.invoke(obj, (Object[]) null);
            }catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    public static Field getMethod(String method, Object obj) {
        Field find;
        
        try {
            find = obj.getClass().getDeclaredField(method);
        } catch (Exception ex) {
            return null;
        }
        
        return find;
    }
    
    public static Object[] getStaticFields(Class<?> c) {
    	Field[] fields = c.getDeclaredFields();
    	ArrayList<Object> returns = new ArrayList<Object>();
    	
    	for (int i = 0; i < fields.length; i++) {
    		if (!Modifier.isPrivate(fields[i].getModifiers())) {
    			try {
    				returns.add(fields[i].get(null));
    			} catch (Exception e) {
    				System.out.println(e.getMessage());
    			}
    		}
    	}
    	
    	return returns.toArray();
    }
    
    //private
    
}
