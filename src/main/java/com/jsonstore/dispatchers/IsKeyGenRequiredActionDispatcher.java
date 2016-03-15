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


import com.jsonstore.security.SecurityManager;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public class IsKeyGenRequiredActionDispatcher extends BaseActionDispatcher {
	private static final String PARAM_USERNAME = "username"; //$NON-NLS-1$

	public IsKeyGenRequiredActionDispatcher(android.content.Context context) {
		super("isKeyGenRequired", context); //$NON-NLS-1$
		addParameter(IsKeyGenRequiredActionDispatcher.PARAM_USERNAME, true, true, JSONStoreParameterType.STRING);
	}
	
	@Override
	public PluginResult actionDispatch(JSONStoreContext js_context) throws Throwable {
		String username = getUserName(js_context);
		int returnCode = (SecurityManager.getInstance(getAndroidContext()).isDPKAvailable(username) ? BaseActionDispatcher.RC_TRUE : BaseActionDispatcher.RC_FALSE);

		return new PluginResult(PluginResult.Status.OK, returnCode);
	}

	private String getUserName(JSONStoreContext context) {
		return context.getStringParameter(IsKeyGenRequiredActionDispatcher.PARAM_USERNAME);
	}

}
