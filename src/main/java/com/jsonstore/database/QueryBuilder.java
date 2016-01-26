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

import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryPartItem;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.util.JSONStoreUtil;

import java.util.Iterator;
import java.util.List;

public abstract class QueryBuilder {


	public enum DeletedInclusion {
		DELETED_AND_NOT_DELETED, NON_DELETED_ONLY, DELETED_ONLY
	};

	private JSONStoreQueryParts queryContent;
	private JSONStoreCollection collectionToSearch;
	private DeletedInclusion deletedInclusion;

	public QueryBuilder(JSONStoreCollection collection, JSONStoreQueryParts content) throws IllegalArgumentException {
		if (collection == null) throw new IllegalArgumentException("collection parameter is null");
		if (content == null) throw new IllegalArgumentException("content parameter cannot be null");
		
		collectionToSearch = collection;
		deletedInclusion = DeletedInclusion.NON_DELETED_ONLY;
		queryContent = content;
	}

	
	public void setSearchDeletedOnly() {
		deletedInclusion = DeletedInclusion.DELETED_ONLY;
	}

	public void setSearchStandard() {
		deletedInclusion = DeletedInclusion.NON_DELETED_ONLY;
	}

	public void setSearchIncludeDeleted() {
		deletedInclusion = DeletedInclusion.DELETED_AND_NOT_DELETED;
	}

	
	private Object convertObjectIfBoolean(Object o) {
		if(o instanceof Boolean) {
			return (Boolean)o == true ? 1 : 0;
		}		
		return o;
	}
	protected void buildFromClause(StringBuilder query_string, List<String> selection_args) throws IllegalArgumentException {
		query_string.append(" " + collectionToSearch.getName() + " ");  //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void handleExactEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.EXACT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_EQ);
		builder.append(DatabaseConstants.SQL_OR);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(DatabaseConstants.SQL_OR);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(DatabaseConstants.SQL_OR);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$

		selection_args.add("" + value);  //$NON-NLS-1$
		selection_args.add("%-@-" + value);  //$NON-NLS-1$
		selection_args.add("%-@-" + value + "-@-%");  //$NON-NLS-1$ //$NON-NLS-2$
		selection_args.add(value + "-@-%");  //$NON-NLS-1$
	}
	
	
	private void handleExactNotEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.EXACT_NOT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_EQ);
		builder.append(DatabaseConstants.SQL_AND);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(DatabaseConstants.SQL_AND);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(DatabaseConstants.SQL_AND);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$

		selection_args.add("" + value);  //$NON-NLS-1$
		selection_args.add("%-@-" + value);  //$NON-NLS-1$
		selection_args.add("%-@-" + value + "-@-%");  //$NON-NLS-1$ //$NON-NLS-2$
		selection_args.add(value + "-@-%");  //$NON-NLS-1$
	}
	
	
	private void handleFuzzyEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_LIKE);

		selection_args.add("%" + value + "%");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	private void handleFuzzyNotEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_NOT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_NOT_LIKE);

		selection_args.add("%" + value + "%");   //$NON-NLS-1$//$NON-NLS-2$
	}
	
	
	private void handleFuzzyLeftEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_LEFT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(DatabaseConstants.SQL_OR);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$
		
		selection_args.add("%" + value);  //$NON-NLS-1$
		selection_args.add("%" + value + "-@-%");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	private void handleFuzzyNotLeftEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_NOT_LEFT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(DatabaseConstants.SQL_AND);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$
		
		selection_args.add("%" + value);  //$NON-NLS-1$
		selection_args.add("%" + value + "-@-%");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	
	private void handleFuzzyRightEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_RIGHT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(DatabaseConstants.SQL_OR);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$
		
		selection_args.add(value + "%");  //$NON-NLS-1$
		selection_args.add("%-@-" + value + "%");  //$NON-NLS-1$ //$NON-NLS-2$
			
	}
	
	
	private void handleFuzzyNotRightEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.FUZZY_NOT_RIGHT_EQUALS) return;
		value = convertObjectIfBoolean(value);
		
		builder.append(" ( ");  //$NON-NLS-1$
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(DatabaseConstants.SQL_AND);
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_LIKE);
		builder.append(" ) ");  //$NON-NLS-1$
		
		selection_args.add(value + "%");  //$NON-NLS-1$
		selection_args.add("%-@-" + value + "%");  //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
	private void handleBetweenCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.BETWEEN) return;

		List<Object> rangeList = (List<Object>)value;
		Object firstVal = convertObjectIfBoolean(rangeList.get(0));
		Object secondVal = convertObjectIfBoolean(rangeList.get(1));
		
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_BETWEEN);	
		
		selection_args.add(firstVal.toString()); 
		selection_args.add(secondVal.toString()); 
		
	}
	
	private void handleNotBetweenCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.NOT_BETWEEN) return;

		List<Object> rangeList = (List<Object>)value;
		Object firstVal = convertObjectIfBoolean(rangeList.get(0));
		Object secondVal = convertObjectIfBoolean(rangeList.get(1));
		
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_BETWEEN);	
		
		selection_args.add(firstVal.toString()); 
		selection_args.add(secondVal.toString()); 
		
	}
	
	
	private void handleInCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.IN) return;

		List<Object> inObjectsArray  = (List<Object>)value;
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_IN);
		builder.append(" ( "); //$NON-NLS-1$
		Iterator<Object> elementIterator = inObjectsArray.iterator();
		while(elementIterator.hasNext()) {
			builder.append(" ?"); //$NON-NLS-1$
			Object currentItem = convertObjectIfBoolean(elementIterator.next());
			selection_args.add(currentItem.toString());
			if(elementIterator.hasNext()) {
				builder.append(","); //$NON-NLS-1$
			}
		}

		builder.append(" )"); //$NON-NLS-1$
		
	}
	
	private void handleNotInCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.NOT_IN) return;

		List<Object> inObjectsArray  = (List<Object>)value;
		builder.append(safe_key);
		builder.append(DatabaseConstants.SQL_NOT_IN);
		builder.append(" ( "); //$NON-NLS-1$
		Iterator<Object> elementIterator = inObjectsArray.iterator();
		while(elementIterator.hasNext()) {
			builder.append(" ?"); //$NON-NLS-1$
			Object currentItem = convertObjectIfBoolean(elementIterator.next());
			selection_args.add(currentItem.toString());
			if(elementIterator.hasNext()) {
				builder.append(","); //$NON-NLS-1$
			}
		}

		builder.append(" )"); //$NON-NLS-1$
		
	}
	
	private void handleGreaterThanCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.GREATER_THAN) return;
		value = convertObjectIfBoolean(value);

		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_GT);

		selection_args.add("" + value);  //$NON-NLS-1$
		
	}
	
	private void handleGreaterThanEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.GREATER_THAN_OR_EQUALS) return;
		value = convertObjectIfBoolean(value);

		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_GTE);

		selection_args.add("" + value);  //$NON-NLS-1$
		
	}
	
	private void handleLessThanCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.LESS_THAN) return;
		value = convertObjectIfBoolean(value);

		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_LT);

		selection_args.add("" + value);  //$NON-NLS-1$
		
	}
	
	private void handleLessThanEqualsCase(StringBuilder builder, List<String> selection_args, QueryPartOperation operation, String safe_key, Object value) {
		if (operation != QueryPartOperation.LESS_THAN_OR_EQUALS) return;
		value = convertObjectIfBoolean(value);

		builder.append(safe_key); 
		builder.append(DatabaseConstants.SQL_LTE);

		selection_args.add("" + value);  //$NON-NLS-1$
		
	}
	
	protected void buildWhereClause(StringBuilder query_string, List<String> selection_args) throws IllegalArgumentException {
		if (query_string == null) throw new IllegalArgumentException("query_string parameter is null");
		if (selection_args == null) throw new IllegalArgumentException("selection_args parameter is null");

		StringBuilder whereBlock = new StringBuilder();
		

		StringBuilder whereBlockClause = new StringBuilder();
		List<JSONStoreQueryPart> queryContentParts = queryContent.getAllQueryParts();
		
		Iterator<JSONStoreQueryPart> queryContentPartIterator = queryContentParts.iterator();
		while (queryContentPartIterator.hasNext()) {
			JSONStoreQueryPart queryContentPart = queryContentPartIterator.next();
			List<JSONStoreQueryPartItem> queryBlockItems = queryContentPart.getQueryBlockItems();
			
			Iterator<JSONStoreQueryPartItem> queryBlockIterator = queryBlockItems.iterator();

			while (queryBlockIterator.hasNext()) {
				JSONStoreQueryPartItem item = queryBlockIterator.next();
				
				QueryPartOperation operation = item.getOperation();
				String safeKey = item.isKeySpecial() ? item.getKey() : "[" + JSONStoreUtil.getDatabaseSafeSearchFieldName(item.getKey()) + "]";  //$NON-NLS-1$ //$NON-NLS-2$
				Object value = item.getValue(); 
				
				//Build the current clause by checking it against all case handlers
				StringBuilder currentClause = new StringBuilder();
				handleExactEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleExactNotEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyNotEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyLeftEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyNotLeftEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyRightEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleFuzzyNotRightEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleBetweenCase(currentClause,selection_args, operation, safeKey, value);
				handleNotBetweenCase(currentClause,selection_args, operation, safeKey, value);
				handleInCase(currentClause,selection_args, operation, safeKey, value);
				handleNotInCase(currentClause,selection_args, operation, safeKey, value);
				handleGreaterThanCase(currentClause,selection_args, operation, safeKey, value);
				handleGreaterThanEqualsCase(currentClause,selection_args, operation, safeKey, value);
				handleLessThanCase(currentClause,selection_args, operation, safeKey, value);
				handleLessThanEqualsCase(currentClause,selection_args, operation, safeKey, value);
				
				if(currentClause.length() > 0) {
					whereBlockClause.append(" ( ");  //$NON-NLS-1$
					whereBlockClause.append(currentClause);
					whereBlockClause.append(" ) ");  //$NON-NLS-1$
				}

				if (queryBlockIterator.hasNext()) {
					whereBlockClause.append(DatabaseConstants.SQL_AND);
				}

			}


			// Combine block queries with OR
			if (queryContentPartIterator.hasNext()) {
				whereBlockClause.append(DatabaseConstants.SQL_OR);
			}
		}

		if (whereBlockClause.length() > 0) {
			whereBlock.append("(");  //$NON-NLS-1$
			whereBlock.append(whereBlockClause);
			whereBlock.append(")");  //$NON-NLS-1$
		}

		// Handle deleted inclusion.
		if (deletedInclusion == DeletedInclusion.NON_DELETED_ONLY) {
			if (whereBlock.length() > 0) whereBlock.append(DatabaseConstants.SQL_AND);
			whereBlock.append(" " + DatabaseConstants.FIELD_DELETED + " = 0 ");  //$NON-NLS-1$//$NON-NLS-2$
		} else if (deletedInclusion == DeletedInclusion.DELETED_ONLY) {
			if (whereBlock.length() > 0) whereBlock.append(DatabaseConstants.SQL_AND);
			whereBlock.append(DatabaseConstants.FIELD_DELETED + " = 1");  //$NON-NLS-1$
		}

		if (whereBlock.length() == 0) whereBlock.append(1);
		query_string.append(whereBlock);
	}

	public abstract void convertToQueryString(StringBuilder query_string, List<String> selection_args) throws IllegalArgumentException;

}
