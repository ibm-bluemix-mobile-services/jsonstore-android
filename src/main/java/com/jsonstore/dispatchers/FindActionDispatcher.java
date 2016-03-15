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
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreFilterException;
import com.jsonstore.exceptions.JSONStoreFindException;
import com.jsonstore.exceptions.JSONStoreInvalidSortObjectException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;


public class FindActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_QUERY_OBJ = "queryObj"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$
	private static final String OPTION_EXACT = "exact"; //$NON-NLS-1$

	public FindActionDispatcher(android.content.Context context) {
		super("find", context); //$NON-NLS-1$

		// Add parameter validation.
		addParameter(FindActionDispatcher.PARAM_QUERY_OBJ, true, JSONStoreParameterType.ARRAY);
		addParameter(FindActionDispatcher.PARAM_OPTIONS, false, true, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		JSONArray queryArray = js_context.getArrayParameter(FindActionDispatcher.PARAM_QUERY_OBJ);
		JSONObject options = js_context.getObjectParameter(FindActionDispatcher.PARAM_OPTIONS);
		
		// Execute the query.
		try {

			// Get the reference to the collection:
			JSONStoreCollection collection = getCollectionInstance();

			// If the collection is null, the collection has not been previously
			// opened
			if (collection == null) {
				return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
			}

			// Get options, if specified.
			JSONStoreFindOptions findOpts = new JSONStoreFindOptions();
			if (options != null) {
				findOpts = new JSONStoreFindOptions(options);
			}
			
			
			if(queryArray == null){
				queryArray = new JSONArray();
				queryArray.put(new JSONObject());
			}
			
			
			boolean exact = false;
			if(options != null && options.optBoolean(OPTION_EXACT) == true) {
				exact = true;
			}
			
			LinkedHashMap<Integer, JSONObject> resultHash = new LinkedHashMap<Integer, JSONObject> ();
			List<JSONObject> filterResults = new ArrayList<JSONObject>();
		

			LinkedHashMap<String, JSONObject> resultsMap = new LinkedHashMap<String, JSONObject>();
			for (int i = 0; i < queryArray.length (); i++) {
				try {
					JSONObject queryObject = queryArray.getJSONObject (i);
					// Iterate through all elements in the query object to build the query block.
					if (queryObject != null) {
						JSONStoreQueryParts queryContent = new JSONStoreQueryParts();
						JSONStoreQueryPart part = new JSONStoreQueryPart();
						Iterator queryObjectIterator = queryObject.keys();
						while (queryObjectIterator.hasNext()) {
							String key = (String) queryObjectIterator.next();
							Object o = queryObject.get(key);
							if(o instanceof Boolean) o = (Boolean)o ? 1 : 0;
							
							if (exact) {
								part.addEqual(key, o.toString());
							} else {
								part.addLike(key, o.toString());
							}
						}
						queryContent.addQueryPart(part);
						List<JSONObject> partialResults = collection.findDocuments(queryContent, findOpts);
						for(JSONObject partialResult : partialResults) {
							String partialID = partialResult.toString();
							if(!(resultsMap.containsKey(partialID))) {
								resultsMap.put(partialID, partialResult);
							}
						}
					}
				} catch (JSONException e) {
					String message = "Error when attempting to find a document. A JSONException occurred.";
					JSONStoreFindException jsException = new JSONStoreFindException(message, e);
					logger.logError(message, jsException);
					throw jsException;
				}
			}	
			
			List<JSONObject> results = new ArrayList<JSONObject>(resultsMap.values());

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