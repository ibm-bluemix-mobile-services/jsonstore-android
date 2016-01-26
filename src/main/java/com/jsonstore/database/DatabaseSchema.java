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

package com.jsonstore.database;

import android.content.Context;

import com.jsonstore.jackson.JacksonSerializedJSONArray;
import com.jsonstore.jackson.JacksonSerializedJSONObject;
import com.jsonstore.util.JSONStoreUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

// TODO: maybe use a custom exception type.

public class DatabaseSchema {
	private String name;
	private TreeMap<String, SearchFieldType> nodes;
	private TreeMap<String, SearchFieldType> safeNodes;
	private TreeMap<String, SearchFieldType> internalNodes;

	public DatabaseSchema(String name) {
		this.name = name;
		this.nodes = new TreeMap<String, SearchFieldType>();
		this.safeNodes = new TreeMap<String, SearchFieldType>();
		this.internalNodes = new TreeMap<String, SearchFieldType>();

		// Add in nodes that are implicitly part of any schema.

		try {
			internalNodes.put(DatabaseConstants.FIELD_DELETED, SearchFieldType.BOOLEAN);
			internalNodes.put(DatabaseConstants.FIELD_DIRTY, SearchFieldType.NUMBER);
			internalNodes.put(DatabaseConstants.FIELD_ID, SearchFieldType.INTEGER);
			internalNodes.put(DatabaseConstants.FIELD_JSON, SearchFieldType.STRING);
			internalNodes.put(DatabaseConstants.FIELD_OPERATION, SearchFieldType.STRING);
		}

		catch (Throwable e) {
			// Ignore, since this can't happen.
		}
	}

	public DatabaseSchema(String name, Map<String, SearchFieldType> search_fields) throws Throwable {
		this(name);

		for (String key : search_fields.keySet()) {
			addSearchField(key, search_fields.get(key));
		}
	}

	public String getName() {
		return this.name;
	}

	public Iterator<String> getSearchFieldIterator() {
		return this.nodes.keySet().iterator();
	}

	public SearchFieldType getSearchFieldType(String name) {
		return this.nodes.get(name);
	}

	private void addSearchField(String name, SearchFieldType type) throws Throwable {
		String nameFixed;

		if (name == null) {
			throw new Throwable("invalid search field (null) specified");
		}

		name = name.trim();

		if (name.equals("") || (name.indexOf("..") != -1) || name.startsWith(".") || name.endsWith(".")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			throw new Throwable("invalid search field (\"" + name + "\") " + "specified");
		}

		nameFixed = name.toLowerCase(Locale.ENGLISH);

		if (this.nodes.containsKey(nameFixed) || this.internalNodes.containsKey(nameFixed)) {
			throw new Throwable("search field with name \"" + name + "\" " + " is used internally and cannot be reused");
		}

		this.nodes.put(nameFixed, type);
		//Used to compare what get from the user (e.g. 'x.y: string')
		//and what's inside the DB (e.g. 'x_y: TEXT')
		this.safeNodes.put(JSONStoreUtil.getDatabaseSafeSearchFieldName(nameFixed), type);
	}

	private String encodeJSONArrayAsString(JSONArray array, String path) throws JSONException {
		int length = array.length();
		Object result = null;
		StringBuilder str = new StringBuilder();

		// This is the case where multiple objects are mapped to the same
		// name... we're supposed to encode it like so:
		// "value1-@-value2-@-..."

		for (int i = 0; i < length; ++i) {
			result = array.get(i);

			if (result instanceof JSONObject) {
				result = locateChildInObject((JSONObject) result, path);
			}

			if (result != null) {
				str.append(result.toString());

				if (i < (length - 1)) {
					str.append("-@-"); //$NON-NLS-1$
				}
			}
		}

		return str.toString();
	}

	@Override
	public boolean equals(Object o) {
		DatabaseSchema other;

		if (!(o instanceof DatabaseSchema)) {
			return false;
		}

		// Make sure all the keys and values are the same.

		other = (DatabaseSchema) o;
		return other.nodes.equals(this.nodes);
	}

	public boolean equals(TreeMap<String, String> schema_compare) {
		if (schema_compare.size() != (this.nodes.size() + this.internalNodes.size())) {
			return false;
		}

		for (String key : schema_compare.keySet()) {
			String safeKey = JSONStoreUtil.getDatabaseSafeSearchFieldName(key);
			SearchFieldType type = null;

			if (this.safeNodes.containsKey(safeKey)) {
				type = this.nodes.get(safeKey);
				if (null == type) {
					type = this.safeNodes.get(safeKey);
				}
			} else {
				type = this.internalNodes.get(safeKey);
			}

			if ((type == null) || !type.getMappedType().equals(schema_compare.get(key))) {
				return false;
			}
		}

		return true;
	}

	private Object getValueFromObjectCaseInsensitive(JSONObject obj, String path) {
		JSONArray keys = obj.names();
		int length;

		if (keys == null) {
			return null;
		}

		length = keys.length();

		for (int i = 0; i < length; ++i) {
			String key = keys.optString(i);

			if (key != null) {
				if (key.toLowerCase(Locale.ENGLISH).equals(path)) {
					return obj.opt(key);
				}
			}
		}

		return null;
	}

	private Object locateChildInObject(JSONObject obj, String path) {
		Object childObj = null;
		int index = path.indexOf('.');

		if (index == -1) {
			// The child value should be directly in the given JSON object.

			childObj = obj.opt(path);

			if (childObj == null) {
				// It could be that this is one of the "additional indexes"
				// objects and we don't map it beforehand.  Therefore, the
				// given path is lowercase and the key in the object may
				// not be, so we have to do a case-insensitive check.

				return getValueFromObjectCaseInsensitive(obj, path);
			}

			if (childObj instanceof JSONArray) {
				try {
					return encodeJSONArrayAsString((JSONArray) childObj, path);
				}

				catch (JSONException e) {
					return null;
				}
			}

			return childObj;
		}

		// Try to recurse into the child object.

		try {
			
			childObj = obj.opt(path.substring(0, index));
			
			if(childObj == null){
				childObj = getValueFromObjectCaseInsensitive(obj, path.substring(0, index));	
			}
			 

			if (childObj instanceof JSONObject) {
				return locateChildInObject((JSONObject) childObj, path.substring(index + 1));
			}

			else if (childObj instanceof JSONArray) {
				return encodeJSONArrayAsString((JSONArray) childObj, path.substring(index + 1));
			}

			else {
				// TODO: must be some other object.  How are we supposed to
				// handle this case?

				return null;
			}
		}

		catch (JSONException e) {
			// Just return null because the child doesn't have to exist even
			// though it's part of the schema.

			return null;
		}
	}

	public Map<String, Object> mapObject(JSONObject obj, JSONObject additional_search_fields) throws Throwable {
		Set<String> keys = this.nodes.keySet();
		JSONObject normalizedObj;
		TreeMap<String, Object> result = new TreeMap<String, Object>();

		// Iterate over all the nodes in this schema and map the given
		// object.

		normalizedObj = normalizeObject(obj);

		for (String key : keys) {
			Object value = locateChildInObject(normalizedObj, key);

			if (value != null) {
				result.put(key, value);
			}

			if (additional_search_fields != null) {
				value = locateChildInObject(additional_search_fields, key);

				if (value != null) {
					result.put(key, value);
				}
			}
		}

		return result;
	}

	private void mergeIntoObject(JSONObject obj, String key, Object value) throws Throwable {
		String childKey;
		JSONObject childObj;
		int index = key.indexOf('.');

		if (index == -1) {
			// We can just store the value directly.

			mergeValues(obj, key, value);

			return;
		}

		// Otherwise, we'll have to recurse.  Find the first child object and
		// merge into it.

		childKey = key.substring(0, index);
		childObj = obj.optJSONObject(childKey);

		if (childObj == null) {
			// The child object doesn't exist so we'll have to create it
			// first.

			childObj = new JacksonSerializedJSONObject();

			obj.put(childKey, childObj);
		}

		mergeIntoObject(childObj, key.substring(index + 1), value);
	}

	private void mergeValues(JSONObject obj, String key, Object value) throws Throwable {
		Object existingValue = obj.opt(key);

		if (existingValue == null) {
			// No existing value, so we can just set the new value directly.

			obj.put(key, value);

			return;
		}

		if (existingValue instanceof JSONArray) {
			JSONArray array = (JSONArray) existingValue;
			int length = array.length();

			// There are already values here, so tack the new value onto the
			// array (but make sure we don't if the same value already
			// exists.

			for (int i = 0; i < length; ++i) {
				if (array.opt(i) == value) {
					return;
				}
			}

			array.put(value);
		}

		else {
			JSONArray array = new JacksonSerializedJSONArray();

			// There's only a single value, so we have to create a new array
			// to contain the existing value and the new value.

			array.put(existingValue);
			array.put(value);

			obj.put(key, array);
		}
	}

	private JSONObject normalizeObject(JSONObject obj) throws Throwable {
		Iterator<?> keys = obj.keys();
		JSONObject result = new JacksonSerializedJSONObject();

		if (keys == null) {
			return result;
		}

		// Iterate over all the keys and merge them into a new object.

		while (keys.hasNext()) {
			String key = (String) keys.next();
			mergeIntoObject(result, key, normalizeOrCopyObject(obj.get(key)));
		}

		return result;
	}

	private Object normalizeOrCopyObject(Object value) throws Throwable {
		if (value instanceof JSONObject) {
			return normalizeObject((JSONObject) value);
		}

		else if (value instanceof JSONArray) {
			JSONArray array = new JacksonSerializedJSONArray();
			JSONArray existingArray = (JSONArray) value;
			int length = existingArray.length();

			for (int i = 0; i < length; ++i) {
				array.put(normalizeOrCopyObject(existingArray.get(i)));
			}

			return array;
		}

		return value;
	}

	public boolean isSchemaMismatched(String dbName, DatabaseSchema requestedSchema, Context context) {
		DatabaseAccessor accessor = null;
		DatabaseManager dbManager = DatabaseManager.getInstance();

		// If the database has already been provisioned and exists, we need
		// to see if the schema we're being asked to provision matches the
		// one we already have defined for the database.

		try {
			accessor = dbManager.getDatabase(dbName);
		}

		catch (Exception e) {
			// The database accessor hasn't been created yet, but we might
			// have created the database table in a different VM execution.
			// Make sure to compare against the existing table (if any).

			return dbManager.checkDatabaseAgainstSchema(context, dbName, requestedSchema);
		}

		// Otherwise, we can compare against an in-memory schema.

		return !accessor.getSchema().equals(requestedSchema);
	}
}
