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
import com.jsonstore.api.JSONStoreFindOptions;
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.api.JSONStoreRemoveOptions;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreFilterException;
import com.jsonstore.exceptions.JSONStoreFindException;
import com.jsonstore.exceptions.JSONStoreRemoveException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;
import com.jsonstore.util.JSONStoreUtil;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RemoveActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String OPTION_IS_ERASE = "isErase";
	private static final String OPTION_EXACT = "exact";
	private static final String PARAM_OPTIONS = "options";
	private static final String PARAM_QUERY_OBJ = "queryObj";

	public RemoveActionDispatcher(android.content.Context context) {
		super("remove", context);
		addParameter(RemoveActionDispatcher.PARAM_QUERY_OBJ, true, JSONStoreParameterType.ARRAY, JSONStoreParameterType.OBJECT);
		addParameter(RemoveActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) throws JSONStoreFindException, JSONStoreFilterException, JSONStoreDatabaseClosedException, JSONException {
		JSONObject options = js_context.getObjectParameter(RemoveActionDispatcher.PARAM_OPTIONS);
		Object queryObj = js_context.getUntypedParameter(RemoveActionDispatcher.PARAM_QUERY_OBJ);

		List<JSONObject> removeJSONObjectList;
		if (queryObj instanceof JSONObject) {
			removeJSONObjectList = new ArrayList<JSONObject>(1);
			removeJSONObjectList.add((JSONObject)queryObj);
		} else {
			removeJSONObjectList = JSONStoreUtil.convertJSONArrayToJSONObjectList((JSONArray) queryObj);
		}

		JSONStoreRemoveOptions removeOptions = new JSONStoreRemoveOptions();
		boolean exact = true;
		if (options != null) {
			boolean isErase = options.optBoolean(RemoveActionDispatcher.OPTION_IS_ERASE, false);
			exact = options.optBoolean(RemoveActionDispatcher.OPTION_EXACT, false);
			removeOptions.setMarkDirty(!isErase);
		}

		JSONStoreCollection col = getCollectionInstance();
		
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}

		HashMap<Integer, Boolean> idsToRemoveUniqueMap = new HashMap<Integer, Boolean>();
		for(JSONObject currentQueryObject : removeJSONObjectList) {
			JSONStoreQueryParts queryContent = new JSONStoreQueryParts();
			JSONStoreQueryPart part = new JSONStoreQueryPart();
			Iterator queryObjectIterator = currentQueryObject.keys();
			while (queryObjectIterator.hasNext()) {
				String key = (String) queryObjectIterator.next();
				Object o = currentQueryObject.get(key);
				if(o instanceof Boolean) o = (Boolean)o ? 1 : 0;
				part.addEqual(key, o.toString());
			}
			queryContent.addQueryPart(part);
			JSONStoreFindOptions findOpts = new JSONStoreFindOptions();
			findOpts.addSearchFilter(DatabaseConstants.FIELD_ID);
			List<JSONObject> partialResults = col.findDocuments(queryContent, findOpts);
			for(JSONObject partialResult : partialResults) {
				idsToRemoveUniqueMap.put(partialResult.getInt(DatabaseConstants.FIELD_ID), true);
			}			
		}
		
		List<Integer> idsToRemove = new ArrayList<Integer>(idsToRemoveUniqueMap.keySet());
		
		try {
			int totalDeleted = col.removeDocumentsById(idsToRemove, removeOptions);
			return new PluginResult(PluginResult.Status.OK, totalDeleted);
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		} catch (JSONStoreRemoveException e) {
			return new JacksonSerializedResult(PluginResult.Status.ERROR, JSONStoreUtil.convertJSONObjectListToJSONArray(e.getFailedObjects()));
		}

	}
}
