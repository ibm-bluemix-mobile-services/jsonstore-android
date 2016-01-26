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




/**
 * @exclude Used internally
 */
public class JSONStoreQueryPartItem {
	
	private String key;
	private boolean isKeySpecial;
	private QueryPartOperation operation;
	private Object val;
			
	
	/**
	 * @exclude Package Private
	 */
	JSONStoreQueryPartItem(String key, boolean is_key_special, QueryPartOperation operation, Object val) throws IllegalArgumentException {
		if(key == null || key.isEmpty()) throw new IllegalArgumentException("key parameter cannot be null or empty");
		if(val == null) throw new IllegalArgumentException("value parameter cannot be null");
		if(operation == null) operation = QueryPartOperation.FUZZY_EQUALS;
		
		this.key = key;
		this.isKeySpecial = is_key_special;
		this.operation = operation;
		this.val = val;
	}
	


	/**
	 * @exclude
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @exclude
	 */
	public boolean isKeySpecial() {
		return isKeySpecial;
	}

	/**
	 * @exclude
	 */
	public QueryPartOperation getOperation() {
		return operation;
	}
	
	/**
	 * @exclude
	 */
	public Object getValue() {
		return val;
	}
}