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


import com.jsonstore.api.JSONStore;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.exceptions.JSONStoreCloseAllException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public class CloseDatabaseActionDispatcher extends BaseActionDispatcher {
	private static final String PARAM_OPTIONS = "options";

	public CloseDatabaseActionDispatcher(android.content.Context context) {
		super("closeDatabase", context);
		addParameter(CloseDatabaseActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult actionDispatch(JSONStoreContext js_context) throws Throwable {

		JSONStore jsStore = JSONStore.getInstance(getAndroidContext());
		try {
			jsStore.closeAll();
		} catch (JSONStoreCloseAllException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_ERROR_CLOSING_ALL);
		} catch (JSONStoreDatabaseClosedException e) {
			//This is technically OK, since all we wanted to do is close the database silently.
		}
		catch(JSONStoreTransactionFailureException e){
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TRANSACTION_FAILURE_DURING_CLOSE_ALL);
		}

		return new PluginResult(PluginResult.Status.OK, BaseActionDispatcher.RC_OK);
	}
}
