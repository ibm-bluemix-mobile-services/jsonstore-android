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
import com.jsonstore.api.JSONStoreReplaceOptions;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreReplaceException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;
import com.jsonstore.util.JSONStoreUtil;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class ReplaceActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String OPTION_IS_REFRESH = "isRefresh";
	private static final String PARAM_OPTIONS = "options";
	private static final String PARAM_TO_UPDATE = "toUpdate";

	public ReplaceActionDispatcher(android.content.Context context) {
		super("replace", context);
		addParameter(ReplaceActionDispatcher.PARAM_TO_UPDATE, true, JSONStoreParameterType.ARRAY, JSONStoreParameterType.OBJECT);
		addParameter(ReplaceActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		JSONObject rawOptions = js_context.getObjectParameter(ReplaceActionDispatcher.PARAM_OPTIONS);
		Object toUpdate = js_context.getUntypedParameter(ReplaceActionDispatcher.PARAM_TO_UPDATE);

		JSONStoreReplaceOptions replaceOptions = new JSONStoreReplaceOptions();
		if (rawOptions != null) {
			replaceOptions.setMarkDirty(!rawOptions.optBoolean(ReplaceActionDispatcher.OPTION_IS_REFRESH));
		}

		JSONStoreCollection col = getCollectionInstance();
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
		
		int totalReplaced = 0;
		try {
			if(toUpdate instanceof JSONObject) {
				col.replaceDocument((JSONObject)toUpdate, replaceOptions);
				totalReplaced = 1;
			} if(toUpdate instanceof JSONArray) {
				List<JSONObject> documentList = JSONStoreUtil.convertJSONArrayToJSONObjectList((JSONArray)toUpdate);
				col.replaceDocuments(documentList, replaceOptions);
				totalReplaced = ((JSONArray)toUpdate).length();
			}
		} catch (JSONStoreReplaceException e) {
			JSONArray failures = new JSONArray();
			Iterator iter =  e.getFailedObjects().iterator();
			while(iter.hasNext()) {
				failures.put((JSONObject)iter.next());
			}			
			return new JacksonSerializedResult(PluginResult.Status.ERROR, (JSONArray) failures);
			
		} catch (JSONStoreDatabaseClosedException e) { 
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}

		return new PluginResult(PluginResult.Status.OK, totalReplaced);

	}
}
