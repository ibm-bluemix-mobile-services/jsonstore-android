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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Options that are used to modify the change operation in JSONStoreCollection.
 */
public class JSONStoreChangeOptions {

	private static final String REPLACE_CRITERIA_KEY = "replaceCriteria"; //$NON-NLS-1$
	private static final String MARK_DIRTY_KEY = "markDirty"; //$NON-NLS-1$
	private static final String ADD_NEW_KEY = "addNew"; //$NON-NLS-1$
	
	private List<String> searchFieldCriteria;
	private boolean markDirty;
	private boolean addNew;

	public JSONStoreChangeOptions() {
		searchFieldCriteria = new LinkedList<String>();
		markDirty = false;
		addNew = false;
	}

	public JSONStoreChangeOptions(JSONObject js_options) throws JSONException {
		this();
		if (js_options == null)
			return;

		if (js_options.has(ADD_NEW_KEY)) {
			setAddNew(js_options.getBoolean(ADD_NEW_KEY));
		}

		if (js_options.has(MARK_DIRTY_KEY)) {
			setMarkDirty(js_options.getBoolean(MARK_DIRTY_KEY));
		}

		if (js_options.has(REPLACE_CRITERIA_KEY)) {
			JSONArray replaceCrit = js_options.optJSONArray(REPLACE_CRITERIA_KEY);
			if (replaceCrit != null) {
				for (int i = 0; i < replaceCrit.length(); i++) {
					addSearchFieldToCriteria(replaceCrit.getString(i));
				}
			}
		}

	}

	/**
	 * Add a search field to the criteria. This appends to an existing list of
	 * criteria and can be called multiple times.
	 * 
	 * @param search_field
	 *            A search field that is used to compare in the database that is
	 *            found in the document.
	 */
	public void addSearchFieldToCriteria(String search_field) {
		if (search_field == null || search_field.isEmpty()){
			throw new IllegalArgumentException("search_key parameter cannot be null or empty");
		}
		
		searchFieldCriteria.add(search_field);
	}

	/**
	 * Set the search field criteria to the list of strings that are passed. Any
	 * existing criteria are replaced.
	 * 
	 * @param search_fields
	 *            The search criteria to use.
	 */
	public void setSearchFieldCriteria(List<String> search_fields) {
		searchFieldCriteria.clear();

		if (search_fields != null) {
			for (String searchField : search_fields) {
				addSearchFieldToCriteria(searchField);
			}
		}
	}

	public List<String> getSearchFieldCriteria() {
		return searchFieldCriteria;
	}

	/**
	 * Enable or disable the mark dirty flag.
	 * 
	 * @param mark_dirty
	 *            If true, the document that is added will be marked dirty when
	 *            it is changed.
	 */
	public void setMarkDirty(boolean mark_dirty) {
		markDirty = mark_dirty;
	}

	/**
	 * Determine if the mark dirty flag is set or not.
	 * 
	 * @return True if the setMarkDirty method has been called to mark the
	 *            document dirty when it is changed.
	 * @see #setMarkDirty(boolean)
	 */
	public boolean isMarkDirty() {
		return markDirty;
	}

	/**
	 * Enable or disable if the document should be added if a previously stored
	 * document could not be found.
	 * 
	 * @param add_new
	 *            If true, the document will be added if a match could not be
	 *            found in the collection.
	 */
	public void setAddNew(boolean add_new) {
		addNew = add_new;
	}

	/**
	 * Determine if the add new flag is set or not.
	 * 
	 * @return True if the document will be added if a match could not be found
	 *         in the collection.
	 * @see #setAddNew(boolean)
	 */
	public boolean isAddNew() {
		return addNew;
	}

}
