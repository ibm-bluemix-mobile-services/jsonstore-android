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
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreFilterException;
import com.jsonstore.exceptions.JSONStoreInvalidSortObjectException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class AdvancedFindActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_QUERY_OBJ = "queryObj"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$

	public AdvancedFindActionDispatcher(android.content.Context context) {
		super("advancedFind", context); //$NON-NLS-1$
		addParameter(AdvancedFindActionDispatcher.PARAM_QUERY_OBJ, true, JSONStoreParameterType.ARRAY);
		addParameter(AdvancedFindActionDispatcher.PARAM_OPTIONS, false, true, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		JSONArray queryArray = js_context.getArrayParameter(AdvancedFindActionDispatcher.PARAM_QUERY_OBJ);
		JSONObject options = js_context.getObjectParameter(AdvancedFindActionDispatcher.PARAM_OPTIONS);
		// Execute the query.
		try {

			// Get the reference to the collection:
			JSONStoreCollection collection = getCollectionInstance();

			// If the collection is null, the collection has not been previously opened
			if (collection == null) {
				return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
			}

			// Get options, if specified.
			JSONStoreFindOptions findOpts = new JSONStoreFindOptions();
			if (options != null) {
				findOpts = new JSONStoreFindOptions(options);
			}

			JSONStoreQueryParts content = new JSONStoreQueryParts(queryArray);
			List<JSONObject> results = collection.findDocuments(content, findOpts);

			return new JacksonSerializedResult(PluginResult.Status.OK, new JSONArray(results));
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);

		} catch (JSONStoreInvalidSortObjectException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_ERROR_INVALID_SORT_OBJECT);

		} catch (JSONStoreFilterException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_ERROR_INVALID_FILTER_ARRAY);

		} catch (Throwable e) {
			String collectionName = getCollectionName();
			if(collectionName == null){
				collectionName = ""; //$NON-NLS-1$
			}
			
			String errorStr = "error while executing find query on " + "database \"" + collectionName + "\"" + e.toString();

			// Some error occurred, so log the issue and return an error result.
			logger.logError(errorStr, e);

			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		}
	}
}