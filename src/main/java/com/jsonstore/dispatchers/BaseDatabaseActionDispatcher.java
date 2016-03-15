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

import android.content.Context;

import com.jsonstore.api.JSONStore;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public abstract class BaseDatabaseActionDispatcher  extends BaseActionDispatcher {

	protected abstract PluginResult databaseActionDispatch(JSONStoreContext jsContext) throws Throwable;
	private String collectionName = ""; //$NON-NLS-1$
	
	public BaseDatabaseActionDispatcher(String name, Context android_context) {
		super(name, android_context);
		addParameter(BaseActionDispatcher.PARAM_DBNAME, true, JSONStoreParameterType.STRING);
	}

	@Override
	public PluginResult actionDispatch(JSONStoreContext js_context) throws Throwable {
		collectionName = js_context.getStringParameter(BaseActionDispatcher.PARAM_DBNAME);
		return databaseActionDispatch(js_context);
	}
	
	protected String getCollectionName() {
		return collectionName;
	}
	
	protected JSONStoreCollection getCollectionInstance() {
        JSONStore jsStore = JSONStore.getInstance (getAndroidContext());
        JSONStoreCollection col = jsStore.getCollectionByName(getCollectionName());
        return col;
	}
}
