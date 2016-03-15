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
import com.jsonstore.exceptions.JSONStoreDestroyFailureException;
import com.jsonstore.exceptions.JSONStoreDestroyFileError;
import com.jsonstore.exceptions.JSONStoreMetadataRemovalFailure;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParameterType;

import org.apache.cordova.PluginResult;

public class DestroyDBFileAndKeychainActionDispatcher extends BaseActionDispatcher {
	private static final String PARAM_USERNAME = "username";
	private static final String PARAM_OPTIONS = "options";

	public DestroyDBFileAndKeychainActionDispatcher(android.content.Context context) {
		super("destroyDbFileAndKeychain", context);
		addParameter(DestroyDBFileAndKeychainActionDispatcher.PARAM_USERNAME, false, JSONStoreParameterType.STRING);
		addParameter(DestroyDBFileAndKeychainActionDispatcher.PARAM_OPTIONS, false, JSONStoreParameterType.OBJECT);
	}

	// TODO: getOptions() if options are ever actually used.

	@Override
	public PluginResult actionDispatch(JSONStoreContext context) {
		
		String username = context.getStringParameter(DestroyDBFileAndKeychainActionDispatcher.PARAM_USERNAME);
		
		try {
			
			JSONStore store = JSONStore.getInstance(getAndroidContext());
			
			if (username != null && !username.equalsIgnoreCase("null") && username.length() > 0) {
				store.destroy(username);
			} else {
				store.destroy();
			}
			
		} catch (JSONStoreDestroyFailureException e) {
			logger.logError("Fail destroying all", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_ERROR_DURING_DESTROY);
		}
		catch(JSONStoreTransactionFailureException e){
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_TRANSACTION_FAILURE_DURING_DESTROY);
		} catch (JSONStoreDestroyFileError e) {
			logger.logError("Fail removing file", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DESTROY_FAILED_FILE_ERROR);
		} catch (JSONStoreMetadataRemovalFailure e) {
			logger.logError("Fail removing metadata", e);
			return new PluginResult(PluginResult.Status.ERROR, DatabaseConstants.RC_DESTROY_FAILED_METADATA_REMOVAL_FAILURE);
		}

		return new PluginResult(PluginResult.Status.OK, BaseActionDispatcher.RC_OK);
	}
}
