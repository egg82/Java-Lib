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

package ninja.egg82.startup;

import ninja.egg82.enums.ServiceType;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.prototype.PrototypeFactory;
import ninja.egg82.registry.Registry;
import ninja.egg82.utils.SettingsLoader;

/**
 *
 * @author egg82
 */

public class Start {
    //vars
    
    //constructor
    public Start() {
        
    }
    
    //events
    
    //public
    public static void init() {
        ServiceLocator.provideService(ServiceType.SETTINGS_LOADER, SettingsLoader.class);
        ServiceLocator.provideService(ServiceType.OPTIONS_REGISTRY, Registry.class, false);
        ServiceLocator.provideService(ServiceType.INIT_REGISTRY, Registry.class);
        ServiceLocator.provideService(ServiceType.PROTOTYPE_FACTORY, PrototypeFactory.class);
        
        /*Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        
        ((ISettingsLoader) ServiceLocator.getService(ServiceType.SETTINGS_LOADER)).loadSave(s + "\\options.json", (IRegistry) ServiceLocator.getService(ServiceType.OPTIONS_REGISTRY));*/
    }
    
    //private
    
}
