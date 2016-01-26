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

import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SortDirection;
import com.jsonstore.exceptions.JSONStoreInvalidSortObjectException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Options that are used to modify the find operation in JSONStoreCollection.
 */
public final class JSONStoreFindOptions {

    private static final String OPTION_LIMIT = "limit"; //$NON-NLS-1$
    private static final String OPTION_OFFSET = "offset"; //$NON-NLS-1$
    private static final String OPTION_SORT_ARRAY = "sort"; //$NON-NLS-1$
    private static final String OPTION_FILTER = "filter"; //$NON-NLS-1$
    
	private Integer limit;
	private Integer offset;
	private LinkedHashMap<String,SortDirection> sort;
	private Map<String, Boolean>  filter;
	
	private boolean includeDeleted = false;
	
	public JSONStoreFindOptions() {
		this.limit = null;
		this.offset = null;
		filter = new HashMap<String, Boolean>();
		sort = new LinkedHashMap<String, SortDirection>();
	}
	
	/**
	 * @exclude Used internally
	 */
	public JSONStoreFindOptions(JSONObject options) throws JSONException, JSONStoreInvalidSortObjectException {
				 

		filter = new HashMap<String, Boolean>();
		sort = new LinkedHashMap<String, SortDirection>();
		
		 String limitStr = options.optString (JSONStoreFindOptions.OPTION_LIMIT, null);
		 if (limitStr != null) {
			 Integer limitParse = Integer.parseInt(limitStr);
			 setLimit(limitParse);
		 }

		 String offsetStr = options.optString (JSONStoreFindOptions.OPTION_OFFSET, null);
		 if (offsetStr != null) {
			 Integer offsetParse = Integer.parseInt(offsetStr);
			 setOffset(offsetParse);
		 }

		 JSONArray sortArray = options.optJSONArray (JSONStoreFindOptions.OPTION_SORT_ARRAY);
		 if (sortArray != null) {
			 for (int idx = 0; idx < sortArray.length (); idx++) {
				 JSONObject sortObject = sortArray.getJSONObject(idx);

				 Iterator<String> keys = sortObject.keys();
				 String key = keys.next();

				 if(keys.hasNext()){
					 throw new JSONStoreInvalidSortObjectException("One of the sort objects in the sort array has more than one field.");
				 }

				 //Parse the direction of the sort for this search field:
				 String sortDirectionStr = sortObject.getString(key);
				 if(sortDirectionStr.equalsIgnoreCase(DatabaseConstants.ASCENDING)){
					 sortBySearchFieldAscending(key);
				 }
				 else if(sortDirectionStr.equalsIgnoreCase(DatabaseConstants.DESCENDING)){
					 sortBySearchFieldDescending(key);
				 }
				 else{
					 throw new JSONStoreInvalidSortObjectException("Invalid sorting direction (ascending or descending) specified.");
				 }
			 }

		 }
		 
		 JSONArray filterParse = options.optJSONArray (JSONStoreFindOptions.OPTION_FILTER);
		 if (filterParse != null) {
			 for(int idx = 0; idx < filterParse.length(); idx++){
				 addSearchFilter(filterParse.getString(idx));
			 }
		 }
	}

	/**
	 * Specify the maximum number of results to be returned.
	 * @param limit
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	/**
	 * Get the maximum number of results as configured.
	 * @return The configured maximum number of results that are returned.
	 */
	public Integer getLimit() {
		return this.limit;
	}
	
	/**
	 * Set the offset of which results will begin.
	 * @param offset The index at where the documents returned will begin.
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	/**
	 * Get the offset index of the results as configured.
	 * @return The index at where the documents returned will begin.
	 */
	public Integer getOffset() {
		return this.offset;
	}

	/**
	 * @exclude Used internally
	 * @return A linked hashmap who's keys are search fields and value is either DESCENDING or ASCENDING.
	 */
	public LinkedHashMap<String,SortDirection> getSort() {
		return sort;
	}

	/**
	 * Add a new search field by which the results will be sorted. The search field will sort the results in a ascending manner against the search field.
	 * Note that it will be sorted in the order that they were added. For example, if you add searchField1 and then searchField2, it will sort
	 * according to searchField1, and then any ties will be sorted according to searchField2.
	 * 
	 * @param search_field The search field that is used for a ascending sort.
	 */
	public void sortBySearchFieldAscending(String search_field) {
		sort.put(search_field, SortDirection.ASCENDING);
	}
	
	/**
	 * Add a new search field by which the results will be sorted. The search field will sort the results in a descending manner against the search field.
	 * Please note that it will be sorted in the order that they were added. For example, if you add searchField1 and then searchField2, it will sort
	 * according to searchField1, and then any ties will be sorted according to searchField2.
	 * 
	 * @param search_field The search field that is used for a descending sort.
	 */
	public void sortBySearchFieldDescending(String search_field) {
		sort.put(search_field, SortDirection.DESCENDING);
	}

	/**
	 * @exclude
	 */
	Map<String, Boolean> getSearchFilters() {
		return filter;
	}

	/**
	 * Set the search fields to be returned on the results of the find operation. 
	 * @param filters array of Strings with the names of the search fields or additional search fields that will be returned.
	 */
	public void setSearchFilters(List<String> filters) {
		this.filter.clear();
		for(String currentFilter : filters) {
			this.filter.put(currentFilter,  false);
		}
	}
	
	/**
	 * Add a search filter to the existing list of search fields.
	 * @param new_filter Add a new search field that must be included in the find results.
	 */
	public void addSearchFilter(String new_filter) {
		filter.put(new_filter, false);
	}
	
	/**
	 * @exclude
	 */
	void addSearchFilterSpecial(String new_filter) {
		filter.put(new_filter, true);
	}
	
	/**
	 * Modify the flag to include deleted documents or to not include deleted documents.
	 * @param include
	 *            When true, documents marked 'deleted' will be included in the find.
	 */
	public void includeDeletedDocuments(boolean include) {
		includeDeleted = include;
	}

	/**
	 * Retrieve the flag that determines if the result should include deleted documents or not include deleted documents.
	 * @return True if configured to include documents marked 'deleted' in the find.
	 */
	public boolean shouldIncludeDeletedDocuments() {
		return includeDeleted;
	}
	
}
