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

package com.jsonstore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JSONStoreLogger {

	private static Logger logger = LoggerFactory.getLogger(JSONStoreLogger.class);
	//private static Logger logger = LogManager.getLogger(JSONStoreLogger.class); //$NON-NLS-1$
//	private static Logger analyticsLogger = LogManager.getLogger("JSONSTORE_ANALYTICS"); //$NON-NLS-1$

	private static boolean analyticsEnabled = false;
	
	//Analytics constants:
	private static final String ANALYTICS_SOURCE = "java"; //$NON-NLS-1$
	private static final String ANALYTICS_RETURN_CODE = "$js.rc"; //$NON-NLS-1$
	private static final String ANALYTICS_OPERATION = "$js.operation"; //$NON-NLS-1$
	private static final String ANALYTICS_COLLECTION = "$js.collection"; //$NON-NLS-1$
	private static final String ANALYTICS_USERNAME = "$js.user"; //$NON-NLS-1$
	private static final String ANALYTICS_END_TIME = "$js.endTime"; //$NON-NLS-1$
	private static final String ANALYTICS_START_TIME = "$js.startTime"; //$NON-NLS-1$
	private static final String ANALYTICS_SOURCE_KEY = "$src"; //$NON-NLS-1$
	private static final String ANALYTICS_SIZE = "$js.size"; //$NON-NLS-1$
	private static final String ANALYTICS_IS_ENCRYPTED = "$js.encryption"; //$NON-NLS-1$

	public static String OPERATION_OPEN = "open"; //$NON-NLS-1$
	public static String OPERATION_CLOSE_ALL = "closeAll"; //$NON-NLS-1$
	public static String OPERATION_CHANGE_PASSWORD = "changePassword"; //$NON-NLS-1$
	public static String OPERATION_DESTROY = "destroy"; //$NON-NLS-1$
	public static String OPERATION_START_TRANSACTION = "startTransaction"; //$NON-NLS-1$
	public static String OPERATION_COMMIT_TRANSACTION = "commitTransaction"; //$NON-NLS-1$
	public static String OPERATION_ROLLBACK_TRANSACTION = "rollbackTransaction"; //$NON-NLS-1$
	public static String OPERATION_ADD = "add"; //$NON-NLS-1$
	public static String OPERATION_IS_DOCUMENT_DIRTY = "isDirty"; //$NON-NLS-1$
	public static String OPERATION_COUNT_ALL_DIRTY = "countAllDirty"; //$NON-NLS-1$
	public static String OPERATION_MARK_CLEAN = "markClean"; //$NON-NLS-1$
	public static String OPERATION_REMOVE_COLLECTION = "removeCollection"; //$NON-NLS-1$
	public static String OPERATION_COUNT = "count"; //$NON-NLS-1$
	public static String OPERATION_REMOVE = "remove"; //$NON-NLS-1$
	public static String OPERATION_REPLACE = "replace"; //$NON-NLS-1$
	public static String OPERATION_FIND = "find"; //$NON-NLS-1$
	public static String OPERATION_CLEAR = "clear"; //$NON-NLS-1$
	public static String OPERATION_CHANGE = "change"; //$NON-NLS-1$
	public static String OPERATION_FIND_ALL_DIRTY = "allDirty"; //$NON-NLS-1$
	
	public static class LogDetails {
		StackTraceElement[] stackTrace;
		Throwable cause;
		long timeLogged;
		String message;	
		String tag;
		String type;
		
		public LogDetails(String tag, String type, String message, Throwable cause) {
			this.tag = tag;
			this.type = type;
			this.message = message;
			this.cause = cause;
			timeLogged = System.currentTimeMillis()/1000;
			stackTrace = Thread.currentThread().getStackTrace();
			
		}
		
		public JSONArray convertStackTraceElementToJSONArray(StackTraceElement[] elements) {
			JSONArray arr = new JSONArray();
			try {
				for(StackTraceElement element : elements) {
					JSONObject elementJSON = new JSONObject();
					elementJSON.put("line", element.getLineNumber()); //$NON-NLS-1$
					elementJSON.put("class", element.getClassName()); //$NON-NLS-1$
					elementJSON.put("method", element.getMethodName()); //$NON-NLS-1$
					elementJSON.put("file", element.getFileName()); //$NON-NLS-1$
					
					arr.put(elementJSON);
				}
			} catch(Throwable t) {
				
			}
			
			return arr;
		}
		public JSONObject convertThrowableToJSONObject(Throwable t) {
			JSONObject throwObj = new JSONObject();
			try {
				JSONArray stackTrace = convertStackTraceElementToJSONArray(t.getStackTrace());
				throwObj.put("throwableStackTrace", stackTrace); //$NON-NLS-1$
				throwObj.put("message", t.getMessage()); //$NON-NLS-1$
				throwObj.put("tag", tag); //$NON-NLS-1$
				throwObj.put("type", type); //$NON-NLS-1$
			} catch (JSONException e) {
				//Don't blow up.
			}
			
			return throwObj;
		}
		
		public JSONObject convertToMetadata() {
			JSONObject metadata = new JSONObject();
			try {
				metadata.put("message", message); //$NON-NLS-1$
				metadata.put("time", timeLogged); //$NON-NLS-1$
				if(cause != null) {
					metadata.put("causedBy", convertThrowableToJSONObject(cause)); //$NON-NLS-1$
				}
				metadata.put("stackTrace", convertStackTraceElementToJSONArray(stackTrace)); //$NON-NLS-1$
			} catch (JSONException e) {
			}
			
			return metadata;
			
		}
		
		
	}
	public static class JSONStoreAnalyticsLogInstance {
		private long startTime;
		private String username;
		private String collection;
		private String operation;

		public JSONStoreAnalyticsLogInstance(String username, String collection, String operation) {
			this.startTime = System.currentTimeMillis();
			this.username = username;
			this.collection = collection;
			this.operation = operation;
		}
	}
     private static final HashMap<String, JSONStoreLogger> instances =
          new HashMap<String, JSONStoreLogger>();
     
     private String tag;
     
     private JSONStoreLogger (String tag) {
          this.tag = tag;
     }
     
     public static synchronized JSONStoreLogger getLogger (String name) {
    	 JSONStoreLogger logger = JSONStoreLogger.instances.get (name);

          if (logger == null) {
               logger = new JSONStoreLogger (name);
               
               JSONStoreLogger.instances.put (name, logger);
          }
          
          return logger;
     }
     
     public void logDebug (String message) {
    	 LogDetails details = new LogDetails(this.tag, "debug", message, null); //$NON-NLS-1$
    	 logger.debug(message, details.convertToMetadata());
     }
     
     public void logDebug (String message, Throwable error) {
    	 LogDetails details = new LogDetails(this.tag, "debug", message, error); //$NON-NLS-1$
    	 logger.debug(message, details.convertToMetadata());
     }
     
     public void logError (String message) {
    	 LogDetails details = new LogDetails(this.tag, "error", message, null); //$NON-NLS-1$
         logger.error(message, details.convertToMetadata());
     }
     
     public void logError (String message, Throwable error) {
    	 LogDetails details = new LogDetails(this.tag, "error",message, error); //$NON-NLS-1$
    	 logger.error(message, details.convertToMetadata(), error);
     }
     
     public void logTrace (String message) {
    	 LogDetails details = new LogDetails(this.tag, "info", message, null); //$NON-NLS-1$
         logger.trace(message, details.convertToMetadata());
     }
     
     public static JSONStoreAnalyticsLogInstance startAnalyticsInstance(String username, String collection, String operation) {
    	 return new JSONStoreAnalyticsLogInstance(username, collection, operation);
     }
     
    //TODO: create a request to send logs (REST-ENDPOINT)
}
