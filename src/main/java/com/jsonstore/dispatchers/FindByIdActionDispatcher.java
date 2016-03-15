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

import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreFindException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class FindByIdActionDispatcher extends BaseDatabaseActionDispatcher {
     private static final String PARAM_IDS = "ids"; //$NON-NLS-1$
     
     public FindByIdActionDispatcher (android.content.Context context) {
          super ("findById", context); //$NON-NLS-1$
          addParameter (FindByIdActionDispatcher.PARAM_IDS, true, JSONStoreParameterType.ARRAY);
     }
     
     @Override
     public PluginResult databaseActionDispatch (JSONStoreContext js_context) throws JSONException {

 		JSONArray jsonIds = js_context.getArrayParameter(FindByIdActionDispatcher.PARAM_IDS);
          
 		List<Integer> ids = new LinkedList<Integer>();
 		for(int i = 0; i < jsonIds.length(); i++) {
 			int id = jsonIds.getInt(i); 			
 			ids.add(id);
 		}
 		
 		 //Get the reference to the collection:
        JSONStoreCollection col = getCollectionInstance();
        
        //If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
        if(col == null) {
        	return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
        }
        
        
        try {
			List<JSONObject> documents = col.findDocumentsById(ids);
			JSONArray resultJSON = new JSONArray();
			for(JSONObject document : documents) {
				resultJSON.put(document);
			}
            return new JacksonSerializedResult(PluginResult.Status.OK, resultJSON);
            
		} catch (JSONStoreDatabaseClosedException e) {
        	return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
        	
		} catch (JSONStoreFindException e) {
			String collectionName = getCollectionName();
			if(collectionName == null){
				collectionName = ""; //$NON-NLS-1$
			}
			
			logger.logError ("error while executing find by ID query " + "on database \"" + collectionName + "\"", e);
			
            return new PluginResult (PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		}
     }
     
}
