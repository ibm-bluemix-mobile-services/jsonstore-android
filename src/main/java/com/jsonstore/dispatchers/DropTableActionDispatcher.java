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
import com.jsonstore.exceptions.JSONStoreRemoveCollectionException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public class DropTableActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$

	public DropTableActionDispatcher(android.content.Context context) {
		super("dropTable", context); //$NON-NLS-1$

		// Add parameter validation.          
		addParameter(DropTableActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) {
		try {
			JSONStoreCollection col = getCollectionInstance();
			if (col == null) {
				return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
			}

			col.removeCollection();
			return new PluginResult(PluginResult.Status.OK, BaseActionDispatcher.RC_OK);

		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		} catch (JSONStoreRemoveCollectionException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_FAILURE);
		}
		catch(JSONStoreTransactionFailureException e){
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TRANSACTION_FAILURE_DURING_REMOVE_COLLECTION);
		}

	}
}
