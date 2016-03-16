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

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;


public class JSONStoreUtil {
    private static final String DATABASE_SPECIAL_CHARACTERS[] = new String[] {
         "@", "$", "^", "&", "|", ">", "<", "?", "-" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$//$NON-NLS-9$
    };

     private static JSONStoreLogger coreLogger = JSONStoreLogger.getLogger("com.jsonstore-core");
     private static JSONStoreLogger dbLogger = JSONStoreLogger.getLogger("com.jsonstore-db");

     private JSONStoreUtil () {
     }
     
     public static JSONStoreLogger getCoreLogger () {
          return coreLogger;
     }

     public static JSONStoreLogger getDatabaseLogger () {
          return dbLogger;
     }
     
     public static String formatString (String pattern, Object... args) {
          return MessageFormat.format (pattern, args);
     }

    public static String getRandomString(int byteLength) {
        byte[] randomByteArray = generateLocalKey(byteLength);
        return encodeBytesAsHexString(randomByteArray);
    }

    public static String encodeBytesAsHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        if(bytes != null) {
            byte[] arr$ = bytes;
            int len$ = bytes.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                byte curByte = arr$[i$];
                result.append(String.format("%02X", new Object[]{Byte.valueOf(curByte)}));
            }
        }

        return result.toString();
    }


    public static byte[] generateLocalKey(int numBytes) {
        byte[] randBytes = new byte[numBytes];
        (new SecureRandom()).nextBytes(randBytes);
        return randBytes;
    }
     
     public static List<List<Integer>> splitListIntoChunks(List<Integer> arr, int chunk_size){
          List<List<Integer>> list = new LinkedList<List<Integer>> ();
          int start = 0;
          int count = arr.size();
          if(count <= chunk_size){
               list.add(arr);
          }else{
               while (count > 0){
                    int numToCopy = Math.min (chunk_size, arr.size() - start);
                    list.add(arr.subList(start,  start + numToCopy));
                    start += chunk_size;
                    count -= chunk_size;
               }
          }
                   
          return list;
          
     }

     public static List<JSONObject> convertJSONArrayToJSONObjectList(JSONArray arr) {
    	 List<JSONObject> results = new LinkedList<JSONObject>();
    	 if(arr != null) {
    		 for(int i = 0; i < arr.length(); i++) {
    			 try {
					results.add(arr.getJSONObject(i));
				} catch (JSONException e) {
					//Do nothing. Ignore this entry
				}
    		 }
    	 }
    	 
    	 return results;
     }
     
     public static List<JSONObject> convertJSONObjectArrayToJSONObjectList(JSONObject[] arr) {
    	 List<JSONObject> results = new LinkedList<JSONObject>();
    	 if(arr != null) {
    		 for(int i = 0; i < arr.length; i++) {
    			 if(arr[i] != null) {
        			 JSONObject obj = arr[i];
        			 results.add(obj);
    				 
    			 }
    		 }
    	 }
    	 
    	 return results;
     }
     
     
     

     public static JSONArray convertJSONObjectListToJSONArray(List<JSONObject> objs) {
     	JSONArray out = new JSONArray();
     	if(objs == null) return out;
     	
     	for(JSONObject obj : objs) {
     		if(obj != null) out.put(obj);
     	}
     	
     	return out;
     }
     
     public static String getDatabaseSafeSearchFieldName (String name) {
         if (name == null) {
              return null;
         }
         
         for (String ch : DATABASE_SPECIAL_CHARACTERS) {
              name = name.replace (ch, ""); //$NON-NLS-1$
         }
         
        // String res = "'" + name.replace ('.', '_') + "'";
         
         String res = name.replace ('.', '_');
         
         return res;
    }

    public static void loadLibrary(Context context, String filename) throws Exception {
        String libPath = context.getApplicationInfo().dataDir + "/lib/";
        String jniLibPath = context.getApplicationInfo().dataDir + "/jniLibs/";

        File libFile = new File(libPath + filename);
        File jniFile = new File(jniLibPath + filename);

        if(libFile.exists()){
            System.load(libPath + filename);
        } else if(jniFile.exists()){
            System.load(jniLibPath + filename);
        } else {
            throw new Exception("Cannot find " + filename + " in either libs or jniLibs");
        }
    }


     
}