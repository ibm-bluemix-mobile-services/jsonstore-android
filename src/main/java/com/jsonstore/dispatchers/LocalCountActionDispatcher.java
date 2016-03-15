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
import com.jsonstore.exceptions.JSONStoreCountException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.types.JSONStoreContext;

import org.apache.cordova.PluginResult;

public class LocalCountActionDispatcher extends BaseDatabaseActionDispatcher {
	
	public LocalCountActionDispatcher(android.content.Context context) {
		super("localCount", context);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		
		//Get the reference to the collection:
		JSONStoreCollection col = getCollectionInstance();

		//If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
		
		try {
			int count = col.countAllDirtyDocuments();
			return new PluginResult(PluginResult.Status.OK, count);
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		} catch (JSONStoreCountException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		}
	}

}
