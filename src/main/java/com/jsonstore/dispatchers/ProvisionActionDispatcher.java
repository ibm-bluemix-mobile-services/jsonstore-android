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
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.api.JSONStoreInitOptions;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreCloseAllException;
import com.jsonstore.exceptions.JSONStoreInvalidPasswordException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.exceptions.JSONStoreSchemaMismatchException;
import com.jsonstore.exceptions.JSONStoreTransactionDuringInitException;
import com.jsonstore.security.SecurityUtils;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProvisionActionDispatcher extends BaseDatabaseActionDispatcher {
	private static final String PBKDF2_ITERATIONS = "pbkdf2Iterations"; //$NON-NLS-1$
	private static final String OPTIONS_SECURE_RANDOM = "secureRandom"; //$NON-NLS-1$
	private static final String OPTION_ANALYTICS = "analytics"; //$NON-NLS-1$
	private static final String OPTION_ADD_INDEXES = "additionalSearchFields"; //$NON-NLS-1$
	private static final String OPTION_DROP_COLLECTION = "dropCollection"; //$NON-NLS-1$
	private static final String OPTION_PASSWORD = "collectionPassword"; //$NON-NLS-1$
	private static final String OPTION_USERNAME = "username"; //$NON-NLS-1$
	private static final String PARAM_OPTIONS = "options"; //$NON-NLS-1$
	private static final String PARAM_SCHEMA = "schema"; //$NON-NLS-1$

	public ProvisionActionDispatcher(android.content.Context context) {
		super("provision", context); //$NON-NLS-1$
		addParameter(ProvisionActionDispatcher.PARAM_SCHEMA, true, JSONStoreParameterType.OBJECT);
		
		// The options parameter is not loggable since it contains a password in the clear
		addParameter(ProvisionActionDispatcher.PARAM_OPTIONS, true, false, JSONStoreParameterType.OBJECT);
	}


	private JSONStoreCollection createCollectionInstance(String collectionName, JSONStoreContext context, JSONObject addIndexes) throws Throwable {
		Map<String, SearchFieldType> searchFields;
		Map<String, SearchFieldType> addlSearchFields = new HashMap<String, SearchFieldType>();

		//Get schema from the context:
		JSONObject schema = context.getObjectParameter(ProvisionActionDispatcher.PARAM_SCHEMA);

		//Get search fields:
		searchFields = parseSearchFields(schema);

		//Get additional search fields:
		if (addIndexes != null) {
			addlSearchFields = parseSearchFields(addIndexes);
		}

		JSONStoreCollection collection = new JSONStoreCollection(collectionName);
		for (String key : searchFields.keySet()) {
			collection.setSearchField(key, searchFields.get(key));
		}

		for (String key : addlSearchFields.keySet()) {
			collection.setAdditionalSearchField(key, addlSearchFields.get(key));
		}

		return collection;
	}

	private Map<String, SearchFieldType> parseSearchFields(JSONObject schema) throws JSONException {
		Map<String, SearchFieldType> searchFields = new HashMap<String, SearchFieldType>();
		JSONArray names = schema.names();
		int length;

		if (names != null) {
			length = names.length();

			for (int i = 0; i < length; ++i) {
				String name = names.getString(i);
				String value = schema.getString(name);
				searchFields.put(name, SearchFieldType.valueOf(value.toUpperCase(Locale.ENGLISH)));
			}
		}

		return searchFields;
	}

	@Override
	public PluginResult databaseActionDispatch(JSONStoreContext context) throws Throwable {
		JSONObject addIndexes = null;
		boolean dropFirst = false;
		String password = null;
		String username = null;
		String secureRandom = null;
		boolean isAnalyticsEnabled = false;
		int pbkdf2Iterations = SecurityUtils.PBKDF2_ITERATIONS;

		JSONObject options = context.getObjectParameter(ProvisionActionDispatcher.PARAM_OPTIONS);

		// Get options, if specified.
		if (options != null) {
			addIndexes = options.optJSONObject(ProvisionActionDispatcher.OPTION_ADD_INDEXES);
			dropFirst = options.optBoolean(ProvisionActionDispatcher.OPTION_DROP_COLLECTION, false);
			password = options.optString(ProvisionActionDispatcher.OPTION_PASSWORD, ""); //$NON-NLS-1$
			username = options.optString(ProvisionActionDispatcher.OPTION_USERNAME, DatabaseConstants.DEFAULT_USERNAME);
			secureRandom = options.optString(OPTIONS_SECURE_RANDOM, null);
			isAnalyticsEnabled = options.optBoolean(OPTION_ANALYTICS, false);
			pbkdf2Iterations = options.optInt(PBKDF2_ITERATIONS, SecurityUtils.PBKDF2_ITERATIONS);

			// Note that we can't log the options parameter until it has been parsed so that we don't log the password in the clear.
			logOptionsObject(addIndexes, dropFirst, password, username);
		}
		try {
			JSONStoreCollection collection = createCollectionInstance(getCollectionName(), context, addIndexes);
			JSONStoreInitOptions storeOpts = new JSONStoreInitOptions(username);
			storeOpts.setPassword(password);
			storeOpts.setClear(dropFirst);
			storeOpts.setSecureRandom(secureRandom);
			storeOpts.setPBKDF2Iterations(pbkdf2Iterations);

			return initializeCollection(collection, storeOpts);
		} catch (IllegalArgumentException e) {
			//Most likely, an invalid search field type was specified.
			logger.logError("Error validating schema", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_FAILURE);
		}

	}

	private PluginResult initializeCollection(JSONStoreCollection collection, JSONStoreInitOptions storeOpts) {
		JSONStore store = JSONStore.getInstance(getAndroidContext());
		try {
			List<JSONStoreCollection> collectionList = new LinkedList<JSONStoreCollection>();
			collectionList.add(collection);			
			store.openCollections(collectionList, storeOpts);
			if (collection.wasReopened()) {
				return new PluginResult(PluginResult.Status.OK, DatabaseConstants.RC_TABLE_EXISTS);
			} else {
				return new PluginResult(PluginResult.Status.OK, DatabaseConstants.RC_TABLE_CREATED);
			}
		} catch (JSONStoreInvalidSchemaException e) {
			// Invalid schema provided, so return an error.
			logger.logError("Error validating schema", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_FAILURE);
		} catch (JSONStoreInvalidPasswordException e) {
			// Password must be invalid.
			//TODO: This is never called. We depend of looking on the error message
			//in the in the outer catch block.
			logger.logError("Error setting key", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_KEY_FAILURE);
		} catch (JSONStoreSchemaMismatchException e) {
			// The database already exists and we're requesting to
			// re-provision it with a different schema, so return an
			// error.
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_SCHEMA_MISMATCH);
		} catch (JSONStoreCloseAllException e) {
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_USERNAME_MISMATCH_DETECTED);
		} 
		catch(JSONStoreTransactionDuringInitException e){
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TRANSACTION_DURING_INIT);
		}
		catch (Throwable e) {
			logger.logError("Error during provision", e);

			String msg = e.getMessage();

			if (msg != null && msg.contains("file is encrypted")) {
				return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_KEY_FAILURE);
			} else {
				return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TABLE_FAILURE);
			}
		}
	}

	private void logOptionsObject(JSONObject addIndexes, boolean dropFirst, String password, String username) {
		logger.logTrace("   " + ProvisionActionDispatcher.OPTION_ADD_INDEXES + "=" + addIndexes);  //$NON-NLS-1$//$NON-NLS-2$
		logger.logTrace("   " + ProvisionActionDispatcher.OPTION_DROP_COLLECTION + "=" + dropFirst); //$NON-NLS-1$ //$NON-NLS-2$
		logger.logTrace("   " + ProvisionActionDispatcher.OPTION_USERNAME + "=" + username);  //$NON-NLS-1$//$NON-NLS-2$

		// Do NOT log the password in the clear, but do log an indication if a password was provided.
		logger.logTrace("   " + ProvisionActionDispatcher.OPTION_PASSWORD + "=" + (password == null || "".equals(password) ? "[]" : "xxxxxxxx")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}
}