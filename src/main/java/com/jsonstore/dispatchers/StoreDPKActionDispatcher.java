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
import com.jsonstore.security.SecurityUtils;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public class StoreDPKActionDispatcher extends BaseActionDispatcher {
	private static final String PBKDF2_ITERATIONS = "pbkdf2Iterations"; //$NON-NLS-1$
	private static final String PARAM_CBK_CLEAR = "cbkClear"; //$NON-NLS-1$
	private static final String PARAM_DPK_CLEAR = "dpkClear"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$
	private static final String PARAM_SALT = "salt"; //$NON-NLS-1$
	private static final String PARAM_USERNAME = "username"; //$NON-NLS-1$
	private static final String PARAM_LOCALKEYGEN = "localKeyGen"; //$NON-NLS-1$

	public StoreDPKActionDispatcher(android.content.Context context) {
		super("storeDPK", context); //$NON-NLS-1$

		// Add parameter validation.
		// None of these parameters are loggable since they are passwords or password related.
		addParameter(StoreDPKActionDispatcher.PARAM_DPK_CLEAR, true, false, JSONStoreParameterType.STRING);
		addParameter(StoreDPKActionDispatcher.PARAM_CBK_CLEAR, true, false, JSONStoreParameterType.STRING);
		addParameter(StoreDPKActionDispatcher.PARAM_SALT, true, false, JSONStoreParameterType.STRING);
		addParameter(StoreDPKActionDispatcher.PARAM_USERNAME, true, true, JSONStoreParameterType.STRING);
		addParameter(StoreDPKActionDispatcher.PARAM_LOCALKEYGEN, true, true, JSONStoreParameterType.BOOLEAN);
		addParameter(PBKDF2_ITERATIONS, false, true, JSONStoreParameterType.INTEGER);

		// This parameter is loggable; it contains no password information
		addParameter(StoreDPKActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	private String getCBKClear(JSONStoreContext context) {
		return context.getStringParameter(StoreDPKActionDispatcher.PARAM_CBK_CLEAR);
	}

	private String getDPKClear(JSONStoreContext context) {
		return context.getStringParameter(StoreDPKActionDispatcher.PARAM_DPK_CLEAR);
	}

	private String getSalt(JSONStoreContext context) {
		return context.getStringParameter(StoreDPKActionDispatcher.PARAM_SALT);
	}

	private String getUserName(JSONStoreContext context) {
		return context.getStringParameter(StoreDPKActionDispatcher.PARAM_USERNAME);
	}

	private Boolean getLocalKeyGen(JSONStoreContext context) {
		return context.getBooleanParameter(StoreDPKActionDispatcher.PARAM_LOCALKEYGEN);
	}

	@Override
	public PluginResult actionDispatch(JSONStoreContext js_context) throws Throwable {

		String cbkClear = getCBKClear(js_context);
		String dpkClear = getDPKClear(js_context);
		String salt = getSalt(js_context);
		String username = getUserName(js_context);
		Boolean localKeyGen = getLocalKeyGen(js_context);
		
		int pbkdf2Iterations;
		Integer pbkdf2IterationsObj = js_context.getIntParameter(StoreDPKActionDispatcher.PARAM_LOCALKEYGEN);
		if(pbkdf2IterationsObj == null){
			pbkdf2Iterations = SecurityUtils.PBKDF2_ITERATIONS;
		}
		else{
			pbkdf2Iterations = pbkdf2IterationsObj;
		}

		SecurityManager.getInstance(getAndroidContext()).storeDPK(cbkClear, username, dpkClear, salt, false, pbkdf2Iterations);

		// TODO: always return OK since we don't have a failure case (it
		// would result in an exception and return error in that case).

		return new PluginResult(PluginResult.Status.OK, BaseActionDispatcher.RC_OK);
	}
}
