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

public enum QueryPartOperation {
	EXACT_EQUALS("equal", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	EXACT_NOT_EQUALS("notEqual", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_EQUALS("like", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_NOT_EQUALS("notLike", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_LEFT_EQUALS("leftLike", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_NOT_LEFT_EQUALS("notLeftLike", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_RIGHT_EQUALS("rightLike", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	FUZZY_NOT_RIGHT_EQUALS("notRightLike", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	LESS_THAN("lessThan", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	LESS_THAN_OR_EQUALS("lessOrEqualThan", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	GREATER_THAN("greaterThan", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	GREATER_THAN_OR_EQUALS("greaterOrEqualThan", QueryPartOperationRestriction.PRIMITIVE_ONLY), //$NON-NLS-1$
	BETWEEN("between", QueryPartOperationRestriction.RANGE_ONLY), //$NON-NLS-1$
	NOT_BETWEEN("notBetween", QueryPartOperationRestriction.RANGE_ONLY), //$NON-NLS-1$
	IN("inside", QueryPartOperationRestriction.ARRAY_ONLY), //$NON-NLS-1$
	NOT_IN("notInside", QueryPartOperationRestriction.ARRAY_ONLY); //$NON-NLS-1$

    
	public enum QueryPartOperationRestriction {
		PRIMITIVE_ONLY,
		RANGE_ONLY,
		ARRAY_ONLY
	}
	
	private String queryString;
	private QueryPartOperationRestriction restriction;
	
	private QueryPartOperation(String name, QueryPartOperationRestriction restriction) {
		this.queryString = name;
		this.restriction = restriction;
	}

	
	public boolean queryStringMatches(String query_key) {
		
		//Handle the case that JS may return a key with a $ in front.
		if(query_key == null) return false;
		if(query_key.equalsIgnoreCase(queryString) || query_key.equalsIgnoreCase("$" + queryString)) return true; //$NON-NLS-1$
		return false;
	}
	
	public QueryPartOperationRestriction getRestriction() {
		return restriction;
	}
}