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
import com.jsonstore.exceptions.JSONStoreMarkCleanException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MarkCleanActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PARAM_DOCS = "docs";
	private static final String PARAM_OPTIONS = "options";

	public MarkCleanActionDispatcher(android.content.Context context) {
		super("markClean", context);
		addParameter(MarkCleanActionDispatcher.PARAM_DOCS, true, JSONStoreParameterType.ARRAY);
		addParameter(MarkCleanActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext js_context) throws JSONException {

		JSONArray docs = js_context.getArrayParameter(MarkCleanActionDispatcher.PARAM_DOCS);
		int numOfCleanDocs = 0;
		
		JSONStoreCollection col = getCollectionInstance();

		//If we did not get the reference to the collection, it means that the collection was not opened (initialized) beforehand:
		if (col == null) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
				
		List<JSONObject> docList = new LinkedList<JSONObject>();
		for(int i = 0; i < docs.length(); i++) {
			docList.add(docs.getJSONObject(i));
		}

		try {
			numOfCleanDocs = col.markDocumentsClean(docList);
		} catch (JSONStoreMarkCleanException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_COULD_NOT_MARK_DOCUMENT_PUSHED);			
		} catch (JSONStoreDatabaseClosedException e) {
			e.printStackTrace();
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DB_NOT_OPEN);
		}
		
		return new PluginResult(PluginResult.Status.OK, numOfCleanDocs);
	}

}
