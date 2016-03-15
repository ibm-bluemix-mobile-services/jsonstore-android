/*
 *     Copyright 2016 IBM Corp.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.jsonstore.dispatchers;

import com.jsonstore.types.ActionDispatcher;
import com.jsonstore.util.JSONStoreLogger;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class DispatchingPlugin extends CordovaPlugin {
     private static final Executor executor = Executors.newFixedThreadPool(1); //1 means that the number of concurrent threads is 1, so they are executed serially, which is what we want
     private static final JSONStoreLogger logger = JSONStoreLogger.getLogger(DispatchingPlugin.class.getName());
     private final HashMap<String, ActionDispatcher> dispatchers;

     public DispatchingPlugin () {
          super();
          this.dispatchers = new HashMap<String, ActionDispatcher>();
     }
     
     @Override
     public boolean execute (final String action, final JSONArray args, final CallbackContext callbackContext) {
    	 ActionDispatcher dispatcher = dispatchers.get (action);

    	 // Schedule the plugin action to be run.
    	 executor.execute (new ActionDispatcherRunnable (dispatcher, args, callbackContext, action));

    	 return true;
     }

     // Nasty code used to ensure that plugin actions are queued up as they
     // arrive and execute in the same order.
     
     private class ActionDispatcherRunnable implements Runnable {
          private JSONArray args;
          private CallbackContext callbackContext;
          private ActionDispatcher dispatcher;
          private String actionName;
          
          private ActionDispatcherRunnable (ActionDispatcher dispatcher, JSONArray args, CallbackContext callbackContext, String actionName) {
               this.args = args;
               this.callbackContext = callbackContext;
               this.dispatcher = dispatcher;
               this.actionName = actionName;
          }

          @Override
          public void run () {
               PluginResult result = null;

               logger.logTrace ("dispatching action \"" + actionName + "\"");
               // We can't log the args because some of them contain passwords in the clear. They must be logged by the individual BaseActionDispather subclasses 

               if (dispatcher == null) {
                    try {
                         callbackContext.sendPluginResult ( new PluginResult(PluginResult.Status.ERROR, "unable to dispatch action \"" + actionName + "\""));
                    }
                    catch (Exception e) {
                         logger.logError ("Could not send plugin result because of an exception");
                    }
               }
               
               try {
                    result = this.dispatcher.dispatch (this.args);
               }
               
               catch (Throwable e) {
            	   logger.logError ("error while dispatching " + "action \"" + this.dispatcher.getName() + "\"", e);
                    
                    result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
               }
               
               try {
                    this.callbackContext.sendPluginResult (result);
               }
               catch (Exception e) {
                    logger.logError("error while sending plugin result to Javascript", e);
               }
          }
     }

     protected void addDispatcher (ActionDispatcher dispatcher) {
          this.dispatchers.put (dispatcher.getName (), dispatcher);          
     }
}
