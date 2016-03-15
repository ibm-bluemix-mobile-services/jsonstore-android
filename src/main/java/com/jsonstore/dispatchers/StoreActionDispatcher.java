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

import com.jsonstore.api.JSONStoreAddOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreAddException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.util.JSONStoreUtil;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class StoreActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String OPTION_IS_ARRAY = "isArray"; //$NON-NLS-1$
	private static final String PARAM_DATA = "data"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$

	public StoreActionDispatcher(android.content.Context context) {
		super("store", context); //$NON-NLS-1$

		// Add parameter validation.
		addParameter(StoreActionDispatcher.PARAM_DATA, true, JSONStoreParameterType.ARRAY, JSONStoreParameterType.OBJECT);
		addParameter(StoreActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {

		Object data = js_context.getUntypedParameter(StoreActionDispatcher.PARAM_DATA);
		JSONObject jsonOptions = js_context.getObjectParameter(StoreActionDispatcher.PARAM_OPTIONS);
		
		int docsStored = 0;
		
		boolean storeEntireArray = false;

		// Get options, if specified.        
		if (jsonOptions != null) {
			storeEntireArray = jsonOptions.optBoolean(StoreActionDispatcher.OPTION_IS_ARRAY, false);
		}
		
		JSONStoreCollection col = getCollectionInstance();
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}

		JSONStoreAddOptions addOptions = new JSONStoreAddOptions(jsonOptions);

		try {
			if (storeEntireArray || (data instanceof JSONObject)) {
				// Store just a single item directly.

				col.addData((JSONObject) data, addOptions);
				docsStored = 1;
			}

			else {
				// Store an array of items.
				List<JSONObject> dataToStore = JSONStoreUtil.convertJSONArrayToJSONObjectList((JSONArray) data);
				
				col.addData(dataToStore, addOptions);
				
				docsStored = dataToStore.size();
			}
		} catch (JSONStoreAddException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.ERROR, e.getAmountAdded());

		} catch (JSONStoreDatabaseClosedException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}

		return new PluginResult(PluginResult.Status.OK, docsStored);
	}
}