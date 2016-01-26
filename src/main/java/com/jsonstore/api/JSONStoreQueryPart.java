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

import com.jsonstore.database.QueryPartOperation;
import com.jsonstore.exceptions.JSONStoreFindException;
import com.jsonstore.util.JSONStoreLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a group of operations that are joined with an AND.
 */
public class JSONStoreQueryPart {
	private static final String EXCEPTION_VALUE_MUST_NOT_BE_NULL = "Value must not be null";
	private static final String EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING = "Search field must be a nonempty string";
	
	private List<JSONStoreQueryPartItem> queryItems = new LinkedList<JSONStoreQueryPartItem>();

	
	public JSONStoreQueryPart() {
		
	}
	
	
	/**
	 * @exclude
	 */
	public JSONStoreQueryPart(JSONObject part) throws JSONStoreFindException {
		
		QueryPartOperation[] operations = QueryPartOperation.values();
		for(int i = 0; i < operations.length; i++) {
			parse(part, operations[i], queryItems);
		}
		
	}
	
	/**
	 * @exclude Package private.
	 */
	void addRawItem(String search_field, boolean is_special, QueryPartOperation operation, Object val) {
		queryItems.add(new JSONStoreQueryPartItem(search_field, is_special, operation, val));
	}
			
	/**
	 * Add LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LIKE statement.
	 * @param val
	 *            Search field's value for the LIKE criteria.
	 */
	public void addLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_EQUALS, val));
	}
	
	/**
	 * Add LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LIKE statement.
	 * @param val
	 *            Search field's value for the LIKE criteria.
	 */
	public void addLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_EQUALS, val));
	}
	
	/**
	 * Add LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LIKE statement.
	 * @param val
	 *            Search field's value for the LIKE criteria.
	 */
	public void addLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_EQUALS, val));
	}
	
	/**
	 * Add NOT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LIKE criteria.
	 */
	public void addNotLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_EQUALS, val));
	}
	
	/**
	 * Add NOT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LIKE criteria.
	 */
	public void addNotLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_EQUALS, val));
	}
	
	/**
	 * Add NOT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LIKE criteria.
	 */
	public void addNotLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_EQUALS, val));
	}
	
	/**
	 * Add RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the RIGHT LIKE criteria.
	 */
	public void addRightLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_RIGHT_EQUALS, val));
	}

	/**
	 * Add RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the RIGHT LIKE criteria.
	 */
	public void addRightLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_RIGHT_EQUALS, val));
	}
	
	/**
	 * Add RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the RIGHT LIKE criteria.
	 */
	public void addRightLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_RIGHT_EQUALS, val));
	}
	
	/**
	 * Add NOT RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT RIGHT LIKE criteria.
	 */
	public void addNotRightLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_RIGHT_EQUALS, val));
	}
	
	/**
	 * Add NOT RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT RIGHT LIKE criteria.
	 */
	public void addNotRightLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_RIGHT_EQUALS, val));
	}
	
	/**
	 * Add NOT RIGHT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT RIGHT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT RIGHT LIKE criteria.
	 */
	public void addNotRightLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_RIGHT_EQUALS, val));
	}
	
	/**
	 * Add LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the LEFT LIKE criteria.
	 */
	public void addLeftLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_LEFT_EQUALS, val));
	}

	/**
	 * Add LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the LEFT LIKE criteria.
	 */
	public void addLeftLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_LEFT_EQUALS, val));
	}
	
	/**
	 * Add LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the LEFT LIKE criteria
	 */
	public void addLeftLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_LEFT_EQUALS, val));
	}
	
	/**
	 * Add NOT LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LEFT LIKE criteria.
	 */
	public void addNotLeftLike(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_LEFT_EQUALS, val));
	}
	
	/**
	 * Add NOT LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LEFT LIKE criteria.
	 */
	public void addNotLeftLike(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_LEFT_EQUALS, val));
	}
	
	/**
	 * Add NOT LEFT LIKE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a NOT LEFT LIKE statement.
	 * @param val
	 *            Search field's value for the NOT LEFT LIKE criteria.
	 */
	public void addNotLeftLike(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.FUZZY_NOT_LEFT_EQUALS, val));
	}
	
	
	

	/**
	 * Add exact EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact EQUAL statement.
	 * @param val
	 *            Search field's value that must be exactly equal in the document.
	 */
	public void addEqual(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_EQUALS, val));
	}
	
	/**
	 * Add exact EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact EQUAL statement.
	 * @param val
	 *            Search field's value that must be exactly equal in the document.
	 */
	public void addEqual(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_EQUALS, val));
	}
	
	/**
	 * Add exact EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact EQUAL statement.
	 * @param val
	 *            Search field's value that must be exactly equal in the document.
	 */
	public void addEqual(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_EQUALS, val));
	}
	
	/**
	 * Add exact NOT EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact NOT EQUAL statement.
	 *            statement
	 * @param val
	 *            Search field's value that must not be exactly equal in the document.
	 */
	public void addNotEqual(String search_field, String val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_NOT_EQUALS, val));
	}
	
	/**
	 * Add exact NOT EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact NOT EQUAL statement.
	 *            statement
	 * @param val
	 *            Search field's value that must not be exactly equal in the document.
	 */
	public void addNotEqual(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_NOT_EQUALS, val));
	}
	
	/**
	 * Add exact NOT EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with an exact NOT EQUAL statement.
	 *            statement
	 * @param val
	 *            Search field's value that must not be exactly equal in the document.
	 */
	public void addNotEqual(String search_field, Boolean val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.EXACT_NOT_EQUALS, val));
	}
	
	/**
	 * Add LESS THAN criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LESS THAN statement.
	 * @param val
	 *            All documents that are returned must have the search field set to a number
	 *            less than this val.
	 */
	public void addLessThan(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.LESS_THAN, val));
	}
	
	/**
	 * Add LESS THAN OR EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a LESS THAN OR EQUALS
	 *            statement.
	 * @param val
	 *            All documents that are returned must have the search field set to a number
	 *            less than or equal to this val.
	 */
	public void addLessThanOrEqual(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.LESS_THAN_OR_EQUALS, val));
	}
	
	/**
	 * Add GREATER THAN criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a GREATER THAN statement.
	 * @param val
	 *            All documents that are returned must have the search field set to a number
	 *            greater than this val.
	 */
	public void addGreaterThan(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.GREATER_THAN, val));
	}
	
	/**
	 * Add GREATER THAN OR EQUAL criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare val against with a GREATER THAN OR EQUAL
	 *            statement.
	 * @param val
	 *            All documents returned must have the search field set to a number
	 *            greater than or equal to this val.
	 */
	public void addGreaterThanOrEqual(String search_field, Number val) {
		if (search_field == null || search_field.isEmpty())
			throw new IllegalArgumentException(EXCEPTION_SEARCH_FIELD_MUST_BE_A_NONEMPTY_STRING);
		if (val == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.GREATER_THAN_OR_EQUALS, val));
	}
	
	/**
	 * Add BETWEEN criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare start and end against with a BETWEEN statement.
	 * @param start
	 *            All documents that are returned must have the search field set to a number
	 *            greater than or equal to this start.
	 * @param end
	 *            All documents that are returned must have the search field set to a number
	 *            less than or equal to this end.
	 */
	public void addBetween(String search_field, Number start, Number end) {
		if (start == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		if (end == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		
		List<Object> rangeList = new ArrayList<Object>(2);
		rangeList.add(start);
		rangeList.add(end);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.BETWEEN, rangeList));
	}
	
	/**
	 * Add a NOT BETWEEN criteria to the query part.
	 * @param search_field
	 *            The search field to compare start and end against with a NOT BETWEEN statement.
	 * @param start
	 *            All documents that are returned must have the search field set to a number
	 *            less than this start.
	 * @param end
	 *            All documents that are returned must have the search field set to a number
	 *            greater than this end.
	 */
	public void addNotBetween(String search_field, Number start, Number end) {
		if (start == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		if (end == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		
		List<Object> rangeList = new ArrayList<Object>(2);
		rangeList.add(start);
		rangeList.add(end);
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.NOT_BETWEEN, rangeList));
	}
	
	/**
	 * Add a INSIDE criteria to the query part.
	 * 
	 * @param search_field
	 *            The search field to compare values against with a IN statement.
	 * @param values
	 *            An non-empty list of numbers, booleans, and/or strings. All matching
	 *            documents match the search field to one of the values in the list.
	 */
	public void addInside(String search_field, List<Object> values) {
		if (values == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		
		ArrayList<Object> valuesChecked = new ArrayList<Object>(values.size());
		
		for(Object value: values) {
			if(value != null && (value instanceof String || value instanceof Number || value instanceof Boolean)) {
				valuesChecked.add(value);
			}
		}
		
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.IN, valuesChecked));
	}
	
	/**
	 * Add a NOT INSIDE criteria to the query part.
	 * @param search_field
	 *            The search field to compare values against with a NOT IN statement.
	 * @param values
	 *            An non-empty list of numbers, booleans, and/or strings. All matching
	 *            documents must not match the search field to one of the values in the
	 *            list.
	 */
	public void addNotInside(String search_field, List<Object> values) {
		if (values == null)
			throw new IllegalArgumentException(EXCEPTION_VALUE_MUST_NOT_BE_NULL);
		
		ArrayList<Object> valuesChecked = new ArrayList<Object>(values.size());
		
		for(Object value: values) {
			if(value != null && (value instanceof String || value instanceof Number || value instanceof Boolean)) {
				valuesChecked.add(value);
			}
		}
		
		queryItems.add(new JSONStoreQueryPartItem(search_field, false, QueryPartOperation.NOT_IN, valuesChecked));
	}
	
	/**
	 * @exclude
	 * @return All query part items for this query part. Used internally.
	 */
	public List<JSONStoreQueryPartItem> getQueryBlockItems() {
		return queryItems;
	}
	
	/**
	 * @exclude
	 */
	private boolean isObjectPrimitive(Object obj) {
		if(obj instanceof JSONArray || obj instanceof JSONObject) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * @exclude
	 */
	private JSONStoreQueryPartItem parseItem(String key, boolean is_key_special, QueryPartOperation operation, Object val) throws JSONStoreFindException {
		try {
			switch(operation.getRestriction()) {
				case ARRAY_ONLY:
					if(!(val instanceof JSONArray)) {
						throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " must be an array",null);
					}
					
					JSONArray valAsArray = (JSONArray)val;
					List<Object> convertedObjectsList = new ArrayList<Object>(valAsArray.length());
					//Convert this to a List<Object>
					for(int i = 0; i < valAsArray.length(); i++) {
						try {
							Object o = valAsArray.get(i);
							
							if(!isObjectPrimitive(o)) {
								throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " at index " + i + "cannot be an JSONArray or JSONObject",null);
							}
							
							convertedObjectsList.add(o);						
						} catch (JSONException e) {
							throw new JSONStoreFindException("Cannot parse query content part. An internal error occured",e);						
						}
					}
					val = convertedObjectsList; //Replace the JSONArray as a List<Object>
					break;
				case RANGE_ONLY:
					if(!(val instanceof JSONArray)) {
						throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " must be an array",null);
					}
					JSONArray arr = (JSONArray)val;
					if(arr.length() != 2) {
						throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " must be an array of size 2",null);
					}
					
					try {
						Object firstNumber = arr.get(0);
						Object secondNumber = arr.get(1);
						
						if(!isObjectPrimitive(firstNumber) || !isObjectPrimitive(secondNumber)) {
							throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " must be an array of size 2 primitives",null);
						}
						
						List<Object> rangeList = new ArrayList<Object>(2);
						rangeList.add(firstNumber);
						rangeList.add(secondNumber);
						val = rangeList;
					} catch (JSONException e) {
						throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " caused an internal error ",e);
						
					}
					break;
				
				case PRIMITIVE_ONLY:
					if(!isObjectPrimitive(val)) {
						throw new JSONStoreFindException("Cannot parse query content part. Tuple with key " + key + " cannot be an array or object",null);
					}
					break;
			} 
		}catch(JSONStoreFindException e) {
			JSONStoreLogger.getLogger("QueryContentPart").logTrace("Invalid syntax: " + e.getMessage()); //$NON-NLS-1$
			return null;
		}
			
		return new JSONStoreQueryPartItem(key, false, operation, val);
		
	}
		
	/**
	 * @exclude
	 */
	private void parseTuple(JSONObject tuple, QueryPartOperation operation, List<JSONStoreQueryPartItem> result) throws JSONStoreFindException {
		//Enforce restrictions:
		if(tuple.length() == 0) {
			throw new JSONStoreFindException("Cannot parse query content part. An internal error occurred parsing part tuple",null);
		}
		Iterator<String> tupleIterator = tuple.keys();
		while(tupleIterator.hasNext()) {
			String key = tupleIterator.next();
			Object val;
			try {
				val = tuple.get(key);
			} catch (JSONException e) {
				throw new JSONStoreFindException("Cannot parse query content query part. An internal error occurred parsing part tuple",e);
			}
			
			JSONStoreQueryPartItem part = parseItem(key, false, operation, val);
			if (part != null)
				result.add(part);
		}
		
	}
	
	/**
	 * @exclude
	 */
	private void parse(JSONObject incoming, QueryPartOperation operation, List<JSONStoreQueryPartItem> results) throws JSONStoreFindException {
		if (incoming == null || operation == null || results == null)
			return;
		
		Iterator<String> incomingKeyIterator = incoming.keys();
		while(incomingKeyIterator.hasNext()) {
			String currentIncomingKey = incomingKeyIterator.next();
			if(operation.queryStringMatches(currentIncomingKey)) {
				JSONArray operationArray = null;
				try {
					operationArray = incoming.getJSONArray(currentIncomingKey);
				} catch(JSONException e) {
					throw new JSONStoreFindException("Cannot parse query content part. " + currentIncomingKey + " query must be a json array.",e);
				}
					
				for(int i = 0; i < operationArray.length(); i++) {
					JSONObject operationTuple = null;
					try {
						operationTuple = operationArray.getJSONObject(i);
					} catch(JSONException e) {
						throw new JSONStoreFindException("Cannot parse query content part. " + currentIncomingKey + " query must contain a JSON object at index " + i,e);
					}
					
					parseTuple(operationTuple, operation,results);
				}
			}
		}
		
	}
	
	
}
