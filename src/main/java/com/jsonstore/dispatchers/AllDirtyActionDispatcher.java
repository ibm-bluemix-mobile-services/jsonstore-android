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
import com.jsonstore.jackson.JacksonSerializedJSONArray;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class AllDirtyActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_SELECTED_DOCS = "selectedDocs";
	
	public AllDirtyActionDispatcher(android.content.Context android_context) {
		super("allDirty", android_context);
		addParameter(AllDirtyActionDispatcher.PARAM_SELECTED_DOCS, false, JSONStoreParameterType.ARRAY, JSONStoreParameterType.OBJECT);
	}


	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) throws JSONException {
		HashSet<Integer> idsToCheck = new HashSet<Integer>();
		JSONArray result = new JacksonSerializedJSONArray();
		Object selectedDocs = js_context.getUntypedParameter(AllDirtyActionDispatcher.PARAM_SELECTED_DOCS);

		if (selectedDocs != null) {
			if (selectedDocs instanceof JSONObject) {
				// Just check against a single object.
				int id = ((JSONObject) selectedDocs).getInt(DatabaseConstants.FIELD_ID);				
				idsToCheck.add(id);
			}

			else {
				JSONArray array = (JSONArray) selectedDocs;
				int length = array.length();

				// This is supposed to be an array of objects to check against.
				for (int i = 0; i < length; ++i) {
					JSONObject obj = array.getJSONObject(i);
					int id = obj.getInt(DatabaseConstants.FIELD_ID);
					idsToCheck.add(id);
				}
			}
		}

		//Get the reference to the collection:
		JSONStoreCollection col = getCollectionInstance();

		//If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}

		try {
			List<JSONObject> actionResult = col.findAllDirtyDocuments();
			for (JSONObject obj : actionResult) {
				if (idsToCheck.size() == 0 || idsToCheck.contains(obj.getInt(DatabaseConstants.FIELD_ID))) {
					result.put(obj);
				}
			}
			return new JacksonSerializedResult(PluginResult.Status.OK, result);
		} catch (JSONStoreFindException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
	}
}
