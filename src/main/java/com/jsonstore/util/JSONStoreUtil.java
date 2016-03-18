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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class JSONStoreUtil {
    private static final String DATABASE_SPECIAL_CHARACTERS[] = new String[] {
         "@", "$", "^", "&", "|", ">", "<", "?", "-" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$//$NON-NLS-6$//$NON-NLS-7$//$NON-NLS-8$//$NON-NLS-9$
    };

     private static JSONStoreLogger coreLogger = JSONStoreLogger.getLogger("com.jsonstore-core");
     private static JSONStoreLogger dbLogger = JSONStoreLogger.getLogger("com.jsonstore-db");
    private static HashSet<String> LOADED_LIBS = new HashSet<String>();
    private static final int LOLLIPOP_MR1 = 22;
    private static final int ANDROID_BUFFER_8K = 8192;

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

    /**
     * Delete a file or directory, including all its children.  The method name "deleteDirectory"
     * is retained for legacy callers, but can take a directory or file as a parameter.
     *
     * @param File The {@link File} object represents the directory to delete.
     * @return true if the directory was deleted, false otherwise.
     */
    public static boolean deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectory(child);
            }
        }
        return fileOrDirectory.delete();
    }

    /**
     * copy input stream to output stream
     * @param in The {@link InputStream} object to be copied from.
     * @param out The {@link OutputStream} object to write to.
     * @throws IOException in case copy fails.
     */
    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        // 8k is the suggest buffer size in android, do not change this
        byte[] buffer = new byte[ANDROID_BUFFER_8K];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }


    public static void unpack(InputStream in, File targetDir) throws IOException {
        ZipInputStream zin = new ZipInputStream(in);

        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            String extractFilePath = entry.getName();
            if (extractFilePath.startsWith("/") || extractFilePath.startsWith("\\")) {
                extractFilePath = extractFilePath.substring(1);
            }
            File extractFile = new File(targetDir.getPath() + File.separator + extractFilePath);
            if (entry.isDirectory()) {
                if (!extractFile.exists()) {
                    extractFile.mkdirs();
                }
                continue;
            } else {
                File parent = extractFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
            }

            // if not directory instead of the previous check and continue
            OutputStream os = new BufferedOutputStream(new FileOutputStream(extractFile));
            copyFile(zin, os);
            os.flush();
            os.close();
        }
    }

    public static final File getNoBackupFilesDir(Context ctx) {
        if(android.os.Build.VERSION.SDK_INT <= LOLLIPOP_MR1)
            return ctx.getFilesDir();
        else
            return ctx.getNoBackupFilesDir();
    }

    /**
     * This method assumes it will find the library at:
     *     files/featurelibs/{arch}/{library}.zip
     *
     * It will unzip the library to the root folder
     * then see if any other architecture folders exist and delete them since
     * they will never be used on this architecture.
     *
     * @param ctx
     * @param library example "libcrypto.so.1.0.0"
     *
     *
     */
    public static final synchronized void loadLib(Context ctx, String library) {

        // keep track of which libs are already loaded, so we don't process multiple calls for the same lib unnecessarily
        // Notice we use a static.  This means calls to loadLib for the same 'library' parameter will be processed
        // only upon app startup, not app foreground.  We want to keep the behavior for cases where the native app has been
        // updated (through the Play Store, for example) and the target .so file needs to be replaced.

        if (!LOADED_LIBS.contains(library)) {

            // we only support "armeabi" and "x86"
            final String ARMEABI = "armeabi";
            final String X86 = "x86";

            String arch = System.getProperty("os.arch");  // the architecture we're running on
            String nonArch = null;  // the architecture we are NOT on
            coreLogger.logDebug("os.arch: " + arch);
            if (arch != null && (arch.toLowerCase().startsWith("i") || arch.toLowerCase().startsWith("x86"))) {  // i686, x86_64
                arch = X86;
                nonArch = ARMEABI;
            } else {
                arch = ARMEABI;
                nonArch = X86;
            }

            final String libPath = arch + File.separator + library;

            File nonArchStorage = new File(getNoBackupFilesDir(ctx), nonArch);

            deleteDirectory(nonArchStorage);

            File targetFile = new File(getNoBackupFilesDir(ctx), library);

            // delete the target
            targetFile.delete();

            coreLogger.logDebug("Extracting zip file: " + libPath);
            try{
                InputStream istr = ctx.getAssets().open(libPath + ".zip");
                unpack(istr, targetFile.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
                coreLogger.logDebug("Error extracting zip file: " + e.getMessage());
            }

            coreLogger.logDebug("Loading library using System.load: " + targetFile.getAbsolutePath());
            System.load(targetFile.getAbsolutePath());
        }

            LOADED_LIBS.add (library);
        }
    }

