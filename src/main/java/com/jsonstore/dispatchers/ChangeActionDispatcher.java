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

import com.jsonstore.api.JSONStoreChangeOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreChangeException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ChangeActionDispatcher extends BaseDatabaseActionDispatcher {
     private static final String PARAM_DATA = "data";
     private static final String PARAM_OPTIONS = "options";
     
     public ChangeActionDispatcher (android.content.Context context) {
          super ("change", context);
          addParameter (ChangeActionDispatcher.PARAM_DATA, true, JSONStoreParameterType.ARRAY);
          addParameter (ChangeActionDispatcher.PARAM_OPTIONS, false, true, JSONStoreParameterType.OBJECT);
     }

     @Override
     public PluginResult databaseActionDispatch(JSONStoreContext js_context) throws JSONException {
          
         Object dataObject = js_context.getUntypedParameter(ChangeActionDispatcher.PARAM_DATA);
         JSONObject jsonOptions = js_context.getObjectParameter(ChangeActionDispatcher.PARAM_OPTIONS);
         
         List<JSONObject> data = new LinkedList<JSONObject>();
         if(dataObject instanceof JSONArray) {
        	 JSONArray dataInputArray = (JSONArray)dataObject;
        	 for(int i = 0; i < dataInputArray.length(); i++) {
        		 data.add((JSONObject)dataInputArray.get(i));
        	 }
        	 
         } else if(dataObject instanceof JSONObject) {
        	 data.add((JSONObject)dataObject);
         } 
            
         JSONStoreChangeOptions changeOptions = new JSONStoreChangeOptions(jsonOptions); 
         
         //Get the reference to the collection:
         JSONStoreCollection col = getCollectionInstance();
         
         //If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
         if(col == null) {
         	return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
         }
         
         try {
			int changeCount = col.changeData(data, changeOptions);
         	return new PluginResult (PluginResult.Status.OK, changeCount);
		} catch (JSONStoreChangeException e) {
         	return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_COULD_NOT_MARK_DOCUMENT_PUSHED);
		} catch (JSONStoreDatabaseClosedException e) {
         	return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
     }
}