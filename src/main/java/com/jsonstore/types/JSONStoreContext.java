/*
 * IBM Confidential OCO Source Materials
 * 
 * 5725-I43 Copyright IBM Corp. 2006, 2013
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 * 
 */

package com.jsonstore.types;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class JSONStoreContext {
	private HashMap<String, Object> parameters;

	public JSONStoreContext() {
		this.parameters = new HashMap<String, Object>();
	}

	protected JSONStoreContext(JSONStoreContext c) {
		this.parameters = c.parameters;
	}

	public JSONArray getArrayParameter(String name) {
		return ((JSONArray) this.parameters.get(name));
	}

	public float getFloatParameter(String name) {
		return ((Float) this.parameters.get(name));
	}

	public Integer getIntParameter(String name) {
		return ((Integer) this.parameters.get(name));
	}

	public JSONObject getObjectParameter(String name) {
		return ((JSONObject) this.parameters.get(name));
	}

	public String getStringParameter(String name) {
		return ((String) this.parameters.get(name));
	}

	public Object getUntypedParameter(String name) {
		return this.parameters.get(name);
	}

	public Boolean getBooleanParameter(String name) {
		return (Boolean) this.parameters.get(name);
	}

	public void addParameter(String name, Object value) {
		this.parameters.put(name, value);
	}
}