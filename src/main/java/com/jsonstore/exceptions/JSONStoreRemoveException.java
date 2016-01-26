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
 * Thrown if the documents that are passed to the remove operation failed to be removed.
 */
public class JSONStoreRemoveException extends JSONStoreException {

	public JSONStoreRemoveException(String message, Throwable source) {
		super(message, source);
		// TODO Auto-generated constructor stub
	}

	public JSONStoreRemoveException(Throwable source) {
		super(source);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = -4642117216934336754L;

	private List<JSONObject> failures;

	public JSONStoreRemoveException(String message) {
		super(message);
		this.failures = new LinkedList<JSONObject>();
	}

	public JSONStoreRemoveException(String message, List<JSONObject> failures) {
		super(message);
		this.failures = failures;
	}

	public List<JSONObject> getFailedObjects() {
		return failures;
	}

}
