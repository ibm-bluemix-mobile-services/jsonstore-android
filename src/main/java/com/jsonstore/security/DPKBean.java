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

package com.jsonstore.security;

import com.jsonstore.jackson.JacksonSerializedJSONObject;
import com.jsonstore.jackson.JsonOrgModule;

import org.json.JSONException;
import org.json.JSONObject;



public class DPKBean {
     private static final String KEY_DPK = "dpk"; //$NON-NLS-1$
     private static final String KEY_ITERATIONS = "iterations"; //$NON-NLS-1$
     private static final String KEY_IV = "iv"; //$NON-NLS-1$
     private static final String KEY_SALT = "jsonSalt"; //$NON-NLS-1$
     private static final String KEY_VERSION = "version"; //$NON-NLS-1$
     private static final String VERSION_NUM = "1.0"; //$NON-NLS-1$
     
     private JSONObject obj;
     
     protected DPKBean (String json) throws JSONException {
          try {
               this.obj = JsonOrgModule.deserializeJSONObject(json);
          }
          
          catch (Throwable e) {
               throw new JSONException(e.getLocalizedMessage());
          }
     }
     
     protected DPKBean (String encryptedDPK, String iv, String salt,
          int iterations) throws JSONException {
          this.obj = new JacksonSerializedJSONObject();
          
          // Fill in the DPK object fields.
          
          this.obj.put (DPKBean.KEY_DPK, encryptedDPK);
          this.obj.put (DPKBean.KEY_ITERATIONS, iterations);
          this.obj.put (DPKBean.KEY_IV, iv);
          this.obj.put (DPKBean.KEY_SALT, salt);
          this.obj.put (DPKBean.KEY_VERSION, DPKBean.VERSION_NUM);
     }
     
     public String getEncryptedDPK () throws JSONException {
          return this.obj.getString (DPKBean.KEY_DPK);
     }
     
     public int getIterations () throws JSONException {
          return this.obj.getInt (DPKBean.KEY_ITERATIONS);
     }
     
     public String getIV () throws JSONException {
          return this.obj.getString (DPKBean.KEY_IV);
     }
     
     public String getSalt () throws JSONException {
          return this.obj.getString (DPKBean.KEY_SALT);
     }
     
     public String getVersion () throws JSONException {
          return this.obj.getString (DPKBean.KEY_VERSION);
     }
     
     public String toString () {
          return this.obj.toString();
     }
}
