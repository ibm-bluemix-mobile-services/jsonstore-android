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

import android.content.Context;
import android.content.SharedPreferences;

import com.jsonstore.database.DatabaseConstants;

import org.json.JSONException;

public class Keychain {

     private static final String PREF_NAME_DPK = "dpk"; //$NON-NLS-1$
     private static final String PREFS_NAME_DPK = "dpkPrefs"; //$NON-NLS-1$
     private SharedPreferences prefs;
     
     protected Keychain (Context context) {
          this.prefs = context.getSharedPreferences (Keychain.PREFS_NAME_DPK,
               Context.MODE_PRIVATE);
     }
     
     public DPKBean getDPKBean (String username) throws JSONException {
          String dpkJSON = this.prefs.getString (buildTag(username), null);
          
          if (dpkJSON == null) {
               return null;
          }
          
          return new DPKBean (dpkJSON);
     }
     
     public boolean isDPKAvailable (String username) {
          return (this.prefs.getString (buildTag(username), null) != null);
     }
     
     public void setDPKBean (String username, DPKBean dpkBean) {
          SharedPreferences.Editor editor = this.prefs.edit();
          
          editor.putString (buildTag(username), dpkBean.toString());
          
          editor.commit();
     }
     
     public void destroy () {
          
          SharedPreferences.Editor editor = this.prefs.edit();
          
          editor.clear();
                    
          editor.commit();
     }
     
     //Builds tags like: dpk-[username]
     //examples: dpk-shin, dpk-shu, dpk (default user)
     private String buildTag (String tag) {
          if (tag.equals (DatabaseConstants.DEFAULT_USERNAME)) {
               //Use pre-2.0 jsonstore dpk keychain key
               return Keychain.PREF_NAME_DPK;
          }
          return Keychain.PREF_NAME_DPK + "-" + tag; //$NON-NLS-1$
     }
}
