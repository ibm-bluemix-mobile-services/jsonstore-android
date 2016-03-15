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
import com.jsonstore.exceptions.JSONStoreChangePasswordException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONObject;

public class ChangePasswordActionDispatcher extends BaseActionDispatcher {
	private static final String PBKDF2_ITERATIONS = "pbkdf2Iterations";
	private static final String PARAM_NEW_PW = "newPW";
	private static final String PARAM_OLD_PW = "oldPW";
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_OPTIONS = "options";

	public ChangePasswordActionDispatcher(android.content.Context context) {
		super("changePassword", context);
		
		// These paramaters are NOT loggable since they contain password information
		addParameter(ChangePasswordActionDispatcher.PARAM_OLD_PW, true, false, JSONStoreParameterType.STRING);
		addParameter(ChangePasswordActionDispatcher.PARAM_NEW_PW, true, false, JSONStoreParameterType.STRING);
		addParameter(ChangePasswordActionDispatcher.PARAM_USERNAME, true, true, JSONStoreParameterType.STRING);

		// This parameter is loggable since it contains no password information
		addParameter(ChangePasswordActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult actionDispatch(JSONStoreContext jsContext) {
		String newPW = jsContext.getStringParameter(ChangePasswordActionDispatcher.PARAM_NEW_PW);
		String oldPW = jsContext.getStringParameter(ChangePasswordActionDispatcher.PARAM_OLD_PW);
		String username = jsContext.getStringParameter(ChangePasswordActionDispatcher.PARAM_USERNAME);
		JSONObject options = jsContext.getObjectParameter(PARAM_OPTIONS);
		
		int pbkdf2Iterations = -1;
		
		if(options != null && options.has(PBKDF2_ITERATIONS)) {
			pbkdf2Iterations = options.optInt(PBKDF2_ITERATIONS);
		}
		
		JSONStore jsStore = JSONStore.getInstance(getAndroidContext());
		
		try {
			if(pbkdf2Iterations <= 0){
				jsStore.changePassword(username, oldPW, newPW);
			}
			else{
				jsStore.changePassword(username, oldPW, newPW, pbkdf2Iterations);
			}
			
		} catch (JSONStoreDatabaseClosedException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		} catch (JSONStoreChangePasswordException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_ERROR_CHANGING_PASSWORD);
		}
		

		return new PluginResult(PluginResult.Status.OK, BaseActionDispatcher.RC_OK);
	}
}
