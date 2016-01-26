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

/**
 * Options that are used to modify the count operation in JSONStoreCollection.
 */
public class JSONStoreCountOptions {

	private boolean includeDeleted;

	public JSONStoreCountOptions() {
		includeDeleted = false;
	}

	/**
	 * Modify the flag to include deleted documents or to not include deleted documents in the count.
	 * @param include
	 *            When true, documents marked 'deleted' will be included in the count.
	 */
	public void includeDeletedDocuments(boolean include) {
		includeDeleted = include;
	}

	/**
	 * Retrieve the flag that determines if the count should include deleted documents or not include deleted documents.
	 * @return True if configured to include documents that are marked 'deleted' in the count.
	 */
	public boolean shouldIncludeDeletedDocuments() {
		return includeDeleted;
	}

}
