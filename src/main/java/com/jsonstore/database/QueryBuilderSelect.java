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
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.util.JSONStoreUtil;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class QueryBuilderSelect extends QueryBuilder {

	
	private Integer limit;
	private Integer offset;
	private Map<String, Boolean> selectStatements;
	private LinkedHashMap<String, SortDirection> sort; 
	
	public QueryBuilderSelect(JSONStoreCollection collection, JSONStoreQueryParts content) {
		super(collection, content);
		generalInit();
	}
	
		
	private void generalInit() {
		limit = null;
		offset = null;
		selectStatements = new LinkedHashMap<String, Boolean>();
		sort = new LinkedHashMap<String,SortDirection>();
	}
	
	public void setLimit(Integer limit) throws IllegalArgumentException {
		this.limit = limit;
	}
	
	public Integer getLimit() {
		return limit;
	}
	
	public void setOffset(Integer offset) throws IllegalArgumentException {
		this.offset = offset;
	}
	
	public Integer getOffset() {
		return offset;
	}
	
	public void addSelectStatement(String statement,Boolean is_special) throws IllegalArgumentException {
		if(statement == null || statement.isEmpty()) throw new IllegalArgumentException("statement parameter is null or empty");
		if(is_special == null) throw new IllegalArgumentException("is_special parameter is null");
		
		selectStatements.put(statement, is_special);
	}
	
	protected Map<String,Boolean> getSelectStatements() {
		return selectStatements;
	}
	
	protected void buildSelectStatement(StringBuilder query_string, List<String> selection_args) throws IllegalArgumentException {
		if(query_string == null) throw new IllegalArgumentException("query_string parameter is null");
		if(selection_args == null) throw new IllegalArgumentException("selection_args parameter is null");
				
		if(selectStatements.isEmpty()) {
			//If no select statements, select all
			selectStatements.put(DatabaseConstants.SQL_ALL,true);
		}
		
		Iterator<String> statementIterator = selectStatements.keySet().iterator();

		query_string.append(" "); //$NON-NLS-1$
		while(statementIterator.hasNext()) {
			
			String statement = statementIterator.next();
			Boolean isSpecial = selectStatements.get(statement);
			
            String safeStatement = statement;
            if(!isSpecial) {
            	safeStatement = "[" + JSONStoreUtil.getDatabaseSafeSearchFieldName(safeStatement) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            
			query_string.append(safeStatement);
			if(statementIterator.hasNext()) {
				query_string.append(", "); //$NON-NLS-1$
			}
		}
		query_string.append(" "); //$NON-NLS-1$
	}
	
	
	protected void buildModifiers(StringBuilder queryString, List<String> selectionArgs) throws IllegalArgumentException {
		if(queryString == null) throw new IllegalArgumentException("queryString parameter is null");
		if(selectionArgs == null) throw new IllegalArgumentException("selectionArgs parameter is null");
		
		Integer limit = getLimit();
		Integer offset = getOffset();
		
		//Handle sort
		if(sort == null){
			sort = new LinkedHashMap<String,SortDirection>();
		}
		if(limit != null && limit < 0) {
			//When offset is less than zero, only sort by _id descending
			sort.clear();
//			sortDirectionsOrder.clear();
			sort.put(DatabaseConstants.FIELD_ID, SortDirection.DESCENDING);
//			sortDirectionsOrder.add(DatabaseConstants.FIELD_ID);
		}
		
		if(sort.size() > 0){
			Iterator<Map.Entry<String, SortDirection>> sortOrderIterator = sort.entrySet().iterator();
			StringBuilder sortBuilder = new StringBuilder();
			while(sortOrderIterator.hasNext()) {
				Map.Entry<String, SortDirection> sortEntry = sortOrderIterator.next();
				String sortKey = sortEntry.getKey();
				SortDirection sortDir = sortEntry.getValue();

				if(sortDir == SortDirection.ASCENDING) {
					sortBuilder.append(","); //$NON-NLS-1$
					sortBuilder.append(" ["+JSONStoreUtil.getDatabaseSafeSearchFieldName(sortKey)+"] ASC "); //$NON-NLS-1$ //$NON-NLS-2$
				} else if(sortDir == SortDirection.DESCENDING) {
					sortBuilder.append(","); //$NON-NLS-1$
					sortBuilder.append(" ["+JSONStoreUtil.getDatabaseSafeSearchFieldName(sortKey)+"] DESC "); //$NON-NLS-1$ //$NON-NLS-2$
				}

			}

			if(sortBuilder.length() > 1) {
				sortBuilder.deleteCharAt(0);
				queryString.append(" " + DatabaseConstants.SQL_SORT + " "); //$NON-NLS-1$ //$NON-NLS-2$
				queryString.append(sortBuilder);
			}
		}
		
		//Handle limit
		if(limit != null) {
			int limitAbs = limit;
			if(limitAbs < 0) limitAbs = -limitAbs;			
			queryString.append (" " + DatabaseConstants.SQL_LIMIT + " " + limitAbs + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		//Handle offset (limit cannot be null)
		if(offset != null && limit != null) {	
			queryString.append (" " + DatabaseConstants.SQL_OFFSET + " " + offset + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	@Override
	public void convertToQueryString(StringBuilder query_string, List<String> selection_args) throws IllegalArgumentException {
		query_string.append(DatabaseConstants.SQL_SELECT);
		buildSelectStatement(query_string,selection_args);

		query_string.append(DatabaseConstants.SQL_FROM);
		buildFromClause(query_string,selection_args);
		
		query_string.append(DatabaseConstants.SQL_WHERE);
		buildWhereClause(query_string,selection_args);
		buildModifiers(query_string,selection_args);
		
	}

	public void clearAllModifiers() {
		limit = null;
		offset = null;
		if(sort != null){
			sort.clear();
		}
	}
	
	public void clearAllSelectStatements() {
		selectStatements.clear();
	}

	public LinkedHashMap<String, SortDirection> getSort() {
		return sort;
	}

	public void setSort(LinkedHashMap<String, SortDirection> sort) {
		this.sort = sort;
	}
}
