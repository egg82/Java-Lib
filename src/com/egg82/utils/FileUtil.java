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

package com.egg82.utils;

import com.google.common.base.Charsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author egg82
 */

public class FileUtil {
    //vars
    
    //constructor
    public FileUtil() {
        
    }
    
    //events
    
    //public
    public static byte[] getContents(String path) {
        Path p = Paths.get(path);
        
        try {
            return Files.readAllBytes(p);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        
        return null;
    }
    public static void putContents(String path, byte[] data) {
        Path p = Paths.get(path);
        
        try {
            Files.write(p, data);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public static String toString(byte[] data) {
        if (data == null) {
            return null;
        }
        
        return new String(data, Charsets.UTF_8);
    }
    public static byte[] toByteArray(String data) {
        if (data == null) {
            return null;
        }
        
        return data.getBytes(Charsets.UTF_8);
    }
    
    //private
    
}
