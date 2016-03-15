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
package com.jsonstore.types;

import com.jsonstore.jackson.JacksonSerializedJSONArray;
import com.jsonstore.jackson.JacksonSerializedJSONObject;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;


// We need this class to make sure PluginResult doesn't call toString() on a
// raw org.json object since it uses a serializer that doesn't perform well.
// TODO: in the future, we should abstract away our JSON implementation...

public class JacksonSerializedResult extends PluginResult {
	public JacksonSerializedResult(PluginResult.Status status, JSONArray message) {
		super(status, new JacksonSerializedJSONArray(message));
	}

	public JacksonSerializedResult(PluginResult.Status status, JSONObject message) {
		super(status, new JacksonSerializedJSONObject(message));
	}
}
