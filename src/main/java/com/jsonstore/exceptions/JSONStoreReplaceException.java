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

package com.jsonstore.exceptions;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Thrown if the documents that are passed to the replace operation failed to be replaced.
 */
public class JSONStoreReplaceException extends JSONStoreException {

	private static final long serialVersionUID = 847694895686168605L;
	
	private List<JSONObject> failures;

	public JSONStoreReplaceException(String message) {
		super(message);
		this.failures = new LinkedList<JSONObject>();
	}

	public JSONStoreReplaceException(String message, List<JSONObject> failures) {
		super(message);
		this.failures = failures;
	}

	/**
	 * @return A list of documents that could not be replaced.
	 */
	public List<JSONObject> getFailedObjects() {
		return failures;
	}


}
