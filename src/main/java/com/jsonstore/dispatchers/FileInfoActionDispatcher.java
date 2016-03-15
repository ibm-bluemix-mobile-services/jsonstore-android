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
import com.jsonstore.api.JSONStoreFileInfo;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.types.JacksonSerializedResult;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class FileInfoActionDispatcher extends BaseActionDispatcher {
	private static final String DISPATCHER_NAME = "fileInfo"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$

	public FileInfoActionDispatcher(android.content.Context context) {
		super(DISPATCHER_NAME, context);

		// This parameter is loggable; it contains no password information
		addParameter(FileInfoActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	// TODO: getOptions() if options are ever actually used.

	@Override
	public PluginResult actionDispatch(JSONStoreContext js_context) throws Throwable {

        JSONStore jsStore = JSONStore.getInstance (getAndroidContext());
        List<JSONStoreFileInfo> fileInfo = jsStore.getFileInfo();
        JSONArray allDatabasesArray = new JSONArray();
        for(JSONStoreFileInfo databaseInfo : fileInfo) {
        	JSONObject result = new JSONObject();
        	result.put("name", databaseInfo.getUsername());
        	result.put("size", databaseInfo.getFileSizeBytes());
        	result.put("isEncrypted", databaseInfo.isEncrypted());
        	allDatabasesArray.put(result);
        }
        
        return new JacksonSerializedResult(PluginResult.Status.OK, allDatabasesArray);
	}
}
