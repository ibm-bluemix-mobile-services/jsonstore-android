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

import com.jsonstore.jackson.JsonOrgModule;
import com.jsonstore.types.ActionDispatcher;
import com.jsonstore.types.JSONStoreContext;
import com.jsonstore.types.JSONStoreParamRequirements;
import com.jsonstore.types.JSONStoreParameterType;
import com.jsonstore.util.JSONStoreLogger;
import com.jsonstore.util.JSONStoreUtil;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;

public abstract class BaseActionDispatcher implements ActionDispatcher {

	protected static final String PARAM_DBNAME = "dbName"; //$NON-NLS-1$

	protected static final int RC_FALSE = 0;
	protected static final int RC_OK = 0;
	protected static final int RC_TRUE = 1;

	protected JSONStoreLogger logger;
	private String name;
	private LinkedList<JSONStoreParamRequirements> paramReqs;
	private Context androidContext;

	public BaseActionDispatcher(String name, Context android_context) {
		this.logger = JSONStoreUtil.getCoreLogger();
		this.name = name;
		this.paramReqs = new LinkedList<JSONStoreParamRequirements>();
		this.androidContext = android_context;
	}

	@Override
	public String getName() {
		return this.name;
	}

	protected void addParameter(String name, boolean required, JSONStoreParameterType... types) {
		this.paramReqs.add(new JSONStoreParamRequirements(name, required, true, types));
	}

	protected void addParameter(String name, boolean required, boolean loggable, JSONStoreParameterType... types) {
		this.paramReqs.add(new JSONStoreParamRequirements(name, required, loggable, types));
	}

	protected Context getAndroidContext() {
		return androidContext;
	}

	@Override
	public PluginResult dispatch(JSONArray args) throws Throwable {

		// Collect and validate the required/optional parameters.

		JSONStoreContext jsContext = new JSONStoreContext();
		collectParameters(jsContext, args);
		
		logger.logTrace("invoking action dispatcher \"" + this.name + "\" with parameters:");

		// IMPORTANT: We can not always log the Parameter values here because some of them
		// contain passwords (for example, the options used by ProvisionActionDispather).
		// Any logging of the sensitive parameter values must be done by the subclass so it can be sure to
		// NOT log sensitive information.
		for (JSONStoreParamRequirements parameter : this.paramReqs) {
			String paramName = parameter.getName();
			if (parameter.isLoggable()) {
				logger.logTrace("   " + paramName + "=" + jsContext.getUntypedParameter(paramName));
			} else {
				logger.logTrace("   " + paramName + "=[value not logged]");
			}
		}

		return actionDispatch(jsContext);
	}

	private void collectParameters(JSONStoreContext params, JSONArray args) throws Throwable {
		int i = 0;

		// Iterate over all the defined parameters and try to find them in
		// the arguments array. If found, validate it and add to the base
		// context parameters.

		for (JSONStoreParamRequirements parameter : this.paramReqs) {
			boolean matched = false;

			for (JSONStoreParameterType expectedType : parameter.getTypes()) {
				Object value = convertParameter(args, i, expectedType);

				if (value != null) {
					matched = true;

					params.addParameter(parameter.getName(), value);

					break;
				}
			}

			if (parameter.isRequired() && !matched) {
				// Missing parameter, so throw an exception.
				// TODO: better exception type?

				throw new Throwable("invalid type for parameter \"" + parameter.getName() + "\" in action dispatcher \"" + getName() + "\"");
			}

			++i;
		}
	}

	private Object convertParameter(JSONArray args, int index, JSONStoreParameterType expectedType) throws Throwable {
		try {
			switch (expectedType) {
			case ARRAY: {
				return JsonOrgModule.deserializeJSONArray(args.getString(index));
			}

			case BOOLEAN: {
				return args.getBoolean(index);
			}

			case DOUBLE: {
				return args.getDouble(index);
			}

			case INTEGER: {
				return args.getInt(index);
			}

			case LONG: {
				return args.getLong(index);
			}

			case OBJECT: {
				return JsonOrgModule.deserializeJSONObject(args.getString(index));
			}

			case STRING: {
				return args.getString(index);
			}

			default: {
				return null;
			}
			}
		}

		catch (JSONException e) {
			// Just return null to indicate failure since there could be
			// more than one expected type.

			return null;
		}
	}

	public abstract PluginResult actionDispatch(JSONStoreContext jsContext) throws Throwable;

}
