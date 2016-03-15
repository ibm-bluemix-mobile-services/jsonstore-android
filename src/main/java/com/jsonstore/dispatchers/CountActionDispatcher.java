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
import com.jsonstore.api.JSONStoreCountOptions;
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class CountActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_QUERY_OBJ = "queryObj"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$
	private static final String OPTION_LIMIT = "limit"; //$NON-NLS-1$
	private static final String OPTION_OFFSET = "offset"; //$NON-NLS-1$
	private static final String OPTION_EXACT = "exact"; //$NON-NLS-1$

	public CountActionDispatcher(android.content.Context context) {
		super("count", context); //$NON-NLS-1$
		addParameter(CountActionDispatcher.PARAM_QUERY_OBJ, false, JSONStoreParameterType.OBJECT);
		addParameter(CountActionDispatcher.PARAM_OPTIONS, false, true, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		// Execute the query.
		JSONObject queryObj = js_context.getObjectParameter(CountActionDispatcher.PARAM_QUERY_OBJ);
		JSONObject options = js_context.getObjectParameter(CountActionDispatcher.PARAM_OPTIONS);

		try {

			//If no options were passed, we still need an empty options object
			if (options == null) {
				options = new JSONObject();
			}

			//Handle exact boolean
			Boolean exact = options.optBoolean(CountActionDispatcher.OPTION_EXACT);

			// Has to be a String because Long will convert to 0
			// if it does not exist
			Integer limit = null; // no limit
			Integer offset = null; // no offset

			String limitStr = null;
			String offsetStr = null;
			// Get options, if specified.
			if (options != null) {
				limitStr = options.optString(CountActionDispatcher.OPTION_LIMIT, null);
				offsetStr = options.optString(CountActionDispatcher.OPTION_OFFSET, null);
			}

			if (limitStr != null) limit = Integer.parseInt(limitStr);
			if (offsetStr != null) offset = Integer.parseInt(offsetStr);

			JSONStoreCollection col = getCollectionInstance();
			if (col == null) return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);

			int count = 0;

			if (queryObj == null) {
				count = col.countAllDocuments();
			} else {
				
				JSONStoreQueryParts queryContent = new JSONStoreQueryParts();
				JSONStoreQueryPart part = new JSONStoreQueryPart();
				Iterator queryObjectIterator = queryObj.keys();
				while (queryObjectIterator.hasNext()) {
					String key = (String) queryObjectIterator.next();
					Object o = queryObj.get(key);
					if(o instanceof Boolean) o = (Boolean)o ? 1 : 0;
					
					if (exact) {
						part.addEqual(key, o.toString());
					} else {
						part.addLike(key, o.toString());
					}
				}
				queryContent.addQueryPart(part);
				
				JSONStoreCountOptions countOptions = new JSONStoreCountOptions();
				countOptions.includeDeletedDocuments(true);
				
				//Convert query object to new query content object
				JSONArray jsonQueryArray = new JSONArray();
				jsonQueryArray.put(queryObj);				
				
				count = col.countDocuments(queryContent, countOptions);
			}

			return new PluginResult(PluginResult.Status.OK, count);
		}

		catch (Throwable e) {
			String collectionName = getCollectionName();
			if(collectionName == null){
				collectionName = ""; //$NON-NLS-1$
			}
			
			String errorStr = "error while executing find query on " + "database \"" + collectionName + "\"";

			// Some error occurred, so log the issue and return an error
			// result.
			logger.logError(errorStr, e);

			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		}
	}
}
