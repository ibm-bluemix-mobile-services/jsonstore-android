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
import com.jsonstore.exceptions.JSONStoreDirtyCheckException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONException;
public class IsDirtyActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_OBJ = "obj"; //$NON-NLS-1$

	public IsDirtyActionDispatcher(android.content.Context context) {
		super("isDirty", context); //$NON-NLS-1$
		addParameter(IsDirtyActionDispatcher.PARAM_OBJ, true, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) throws JSONException {
		int id = js_context.getObjectParameter(IsDirtyActionDispatcher.PARAM_OBJ).getInt(DatabaseConstants.FIELD_ID);

		//Get the reference to the collection:
		JSONStoreCollection col = getCollectionInstance();

		//If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
		
		try {
			boolean isDirty = col.isDocumentDirty(id);
			return new PluginResult(PluginResult.Status.OK, isDirty ? 1 : 0);
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		} catch (JSONStoreDirtyCheckException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_INVALID_SEARCH_FIELD);
		}
		
		
	}
}
