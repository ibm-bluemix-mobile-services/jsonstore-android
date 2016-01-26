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

package com.jsonstore.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;


/**
 * Options that are used to modify the add operation in JSONStoreCollection.
 */
public class JSONStoreAddOptions {


	private static final String OPTION_ADDITIONAL_SEARCH_FIELDS = "additionalSearchFields"; //$NON-NLS-1$
	private static final String OPTION_IS_ADD = "isAdd"; //$NON-NLS-1$
	
	private JSONObject additionalSearchFields;
	private boolean markDirty;

	public JSONStoreAddOptions() {
		additionalSearchFields = new JSONObject();
		markDirty = false;
	}
	
	
	public JSONStoreAddOptions(JSONObject json) {
		this();
		if (json != null) {
			additionalSearchFields = json.optJSONObject(OPTION_ADDITIONAL_SEARCH_FIELDS);
			markDirty = json.optBoolean(OPTION_IS_ADD, false);
		}
	}
	
	/**
	 * @exclude
	 */
	private void putAdditionalSearchField(String key, Object value) throws IllegalArgumentException {
		if (key == null || key.isEmpty()) throw new IllegalArgumentException("Key parameter cannot be null or empty.");
		if (value == null) throw new IllegalArgumentException("Value parameter cannot be null.");

		if (!(value instanceof String || value instanceof Integer || value instanceof Number || value instanceof Boolean)) {
			throw new IllegalArgumentException("Value parameter must be a String, Integer, Number, or Boolean type.");
		}

		if (additionalSearchFields.has(key)) {
			additionalSearchFields.remove(key);
		}

		try {
			additionalSearchFields.put(key, value);
		} catch (JSONException e) {
			// Not important. All passing conditions met at this point. 
			// Still, log exception if this is thrown.
			e.printStackTrace();
		}
	}

	/**
	 * Add an additional search field for the add operation.
	 * @param additional_search_fields
	 *             A JSONObject that contains key/value pairs for additional search fields.
	 * @throws IllegalArgumentException
	 *             Thrown if the additional_search_fields parameter is null or empty.
	 */
	public void addAdditionalSearchFields(JSONObject additional_search_fields) {
		if (additional_search_fields == null) additional_search_fields = new JSONObject();
		Iterator jsonObjectKeys = additional_search_fields.keys();
		while (jsonObjectKeys.hasNext()) {
			String key = (String) jsonObjectKeys.next();
			Object val = null;
			try {
				val = additional_search_fields.get(key);
			} catch (JSONException e) {
				throw new IllegalArgumentException("Error when adding additional search field. Could not get the value in JSONObject for search field '" + key + "'.", e);
			}

			putAdditionalSearchField(key, val);
		}
	}

	/**
	 * Add multiple additional search fields for the add operation.
	 * @param additional_search_fields
	 *             A Map that contains key/value pairs for additional search fields.
	 * @throws IllegalArgumentException
	 *             Thrown if the additional_search_fields parameter is null or empty.
	 */
	public void addAdditionalSearchFields(Map<String, Object> additional_search_fields) {
		for (String key : additional_search_fields.keySet()) {
			try {
				putAdditionalSearchField(key, additional_search_fields.get(key));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error when adding additional search field '" + key + "'.", e);
			}
		}
	}

	/**
	 * Add an additional search field for the add operation.
	 * @param key
	 *             The key of additional search field to include with the document.
	 * @param value
	 *             The value of additional search field for 'key' to include with the
	 *             document.
	 * @throws IllegalArgumentException
	 *             Thrown if the key or value parameter is null.
	 */
	public void addAdditionalSearchField(String key, String value) throws IllegalArgumentException {
		putAdditionalSearchField(key, value);
	}

	/**
	 * Add an additional search field for the add operation.
	 * @param key
	 *             The key of additional search field to include with the document.
	 * @param value
	 *             The value of additional search field for 'key' to include with the
	 *             document.
	 * @throws IllegalArgumentException
	 *             Thrown if the key or value parameter is null.
	 */
	public void addAdditionalSearchField(String key, Integer value) throws IllegalArgumentException {
		putAdditionalSearchField(key, value);
	}

	/**
	 * Add an additional search field for the add operation.
	 * @param key
	 *             The key of additional search field to include with the document.
	 * @param value
	 *             The value of additional search field for 'key' to include with the
	 *             document.
	 * @throws IllegalArgumentException
	 *             Thrown if the key or value parameter is null.
	 */
	public void addAdditionalSearchField(String key, Number value) throws IllegalArgumentException {
		putAdditionalSearchField(key, value);
	}

	/**
	 * Add an additional search field for the add operation.
	 * @param key
	 *             The key of additional search field to include with the document.
	 * @param value
	 *             The value of additional search field for 'key' to include with the
	 *             document.
	 * @throws IllegalArgumentException
	 *             Thrown if the key or value parameter is null.
	 */
	public void addAdditionalSearchField(String key, Boolean value) {
		putAdditionalSearchField(key, value);
	}

	/**
	 * Enable or disable the mark dirty flag.
	 * @param mark_dirty
	 *            If true, the document that is added will be marked dirty when it is added.
	 */
	public void setMarkDirty(boolean mark_dirty) {
		markDirty = mark_dirty;
	}

	/**
	 * Determine if the mark dirty flag is set or not.
	 * @return True if the setMarkDirty method has been called to mark the document
	 *            dirty when it is added.
	 * @see #setMarkDirty(boolean)
	 */
	public boolean isMarkDirty() {
		return markDirty;
	}

	/**
	 * @exclude Package private
	 */
	public JSONObject getAdditionalSearchFieldsAsJSON() {
		return additionalSearchFields;
	}
}
