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

import com.jsonstore.exceptions.JSONStoreFindException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of query parts that are joined with an OR statement.
 */
public class JSONStoreQueryParts {
	private List<JSONStoreQueryPart> parts;

	public JSONStoreQueryParts() {
		parts = new ArrayList<JSONStoreQueryPart>();
	}

	/**
	 * @exclude
	 */
	public JSONStoreQueryParts(JSONArray query) throws JSONStoreFindException {
		if (query == null) {
			parts = new ArrayList<JSONStoreQueryPart>();
		} else {
			parts = new ArrayList<JSONStoreQueryPart>(query.length());
			for (int i = 0; i < query.length(); i++) {
				JSONObject queryPart = null;
				try {
					queryPart = query.getJSONObject(i);
				} catch (JSONException e) {
					throw new JSONStoreFindException("Value in query at index " + i + " is not a JSON Object", e);
				}

				parts.add(new JSONStoreQueryPart(queryPart));
			}

		}
	}

	/**
	 * Add a JSONStoreQueryPart to the group of parts.
	 * @param part
	 *            A query part that should be included in the query. All query parts are
	 *            ORed together. For example, when adding query part A and query part B to
	 *            the query content, the final query will be A OR B.
	 */
	public void addQueryPart(JSONStoreQueryPart part) {
		if (part != null) {
			parts.add(part);
		}
	}

	/**
	 * Retrieve the current list of query parts in this query part group.
	 * @return A list of all query parts in the query content.
	 */
	public List<JSONStoreQueryPart> getAllQueryParts() {
		return parts;
	}
}
