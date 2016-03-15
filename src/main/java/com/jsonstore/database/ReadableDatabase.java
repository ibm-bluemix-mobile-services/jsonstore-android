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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jsonstore.util.JSONStoreLogger;
import com.jsonstore.util.JSONStoreUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;


public class ReadableDatabase {
	private static final String JSONSTORE_DB = "com.jsonstore-db"; //$NON-NLS-1$
	private static final String SQL_AND = " AND "; //$NON-NLS-1$
	private static final String SQL_EQ = " = ?"; //$NON-NLS-1$
	private static final String SQL_FIND = "SELECT {0} FROM {1};"; //$NON-NLS-1$
	private static final String SQL_FIND_BY_ID = "SELECT {0}, {1} FROM {2} WHERE {3} AND _deleted = 0"; //$NON-NLS-1$
	private static final String SQL_FIND_OP = "SELECT {0} FROM {1} WHERE {2} LIKE ?"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE = "SELECT {0} FROM {1} WHERE {2};"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE_WITH_LIMIT = "SELECT {0} FROM {1} WHERE {2} LIMIT {3};"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE_WITH_NEGATIVE_LIMIT = "SELECT {0} FROM {1} WHERE {2} ORDER BY {3} DESC LIMIT {4};"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET = "SELECT {0} FROM {1} WHERE {2} LIMIT {3} OFFSET {4};"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE_WITH_LIMIT_AND_ORDER = "SELECT {0} FROM {1} WHERE {2} ORDER BY {3} LIMIT {4};"; //$NON-NLS-1$
	private static final String SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET_AND_ORDER = "SELECT {0} FROM {1} WHERE {2} ORDER BY {3} LIMIT {4} OFFSET {5};"; //$NON-NLS-1$
	private static final String SQL_LIKE = " LIKE ?"; //$NON-NLS-1$
	private static final String SQL_OR = " OR "; //$NON-NLS-1$

	private SQLiteDatabase database;
	protected JSONStoreLogger logger;
	private DatabaseSchema schema;

	protected ReadableDatabase(SQLiteDatabase database, DatabaseSchema schema) {
		this.database = database;
		this.logger = JSONStoreUtil.getDatabaseLogger();
		this.schema = schema;
	}

	protected SQLiteDatabase getDatabase() {
		return this.database;
	}

	protected DatabaseSchema getSchema() {
		return this.schema;
	}

	public Cursor findByIds(int... ids) {
		StringBuilder idsStr = new StringBuilder();
		String selections[] = new String[ids.length];

		for (int i = 0; i < ids.length; ++i) {
			idsStr.append(DatabaseConstants.FIELD_ID);
			idsStr.append(ReadableDatabase.SQL_EQ);

			if (i < (ids.length - 1)) {
				idsStr.append(ReadableDatabase.SQL_OR);
			}

			selections[i] = "" + ids[i]; //$NON-NLS-1$
		}

		return rawQuery(JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_BY_ID, DatabaseConstants.FIELD_ID, DatabaseConstants.FIELD_JSON, this.schema.getName(), idsStr.toString()), selections);
	}

	protected String findOperationForObjectById(int id) {
		Cursor cursor = rawQuery(JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_OP, DatabaseConstants.FIELD_OPERATION, this.schema.getName(), DatabaseConstants.FIELD_ID), new String[] { "" + id }); //$NON-NLS-1$
		String result;

		if (cursor.getCount() < 1) {
			cursor.close();

			return null;
		}

		cursor.moveToNext();

		result = cursor.getString(0);

		cursor.close();

		return result;
	}

	/*
	 * Find a collection of objects based on a provided set of query params.
	 * 
	 * @param queryObj JSONObject collection of field attribute keys to return in the query
	 * 
	 * @param fieldsToSelect String list of the db fields to be selected/returned from the query
	 * 
	 * @param extraWhereClauses String list
	 * 
	 * @returns Cursor db cursor containing the returned set of matches.
	 * 
	 * @throws JSONException
	 */
	public Cursor findUsingQueryObject(JSONObject queryObj, String fieldsToSelect[], String extraWhereClauses[], String limit, String offset, Boolean exact, String sortString) throws JSONException {
		StringBuilder fieldSelect = new StringBuilder();
		int length = queryObj.length();
		ArrayList<String> selectionArgs = new ArrayList<String>();
		String sql = null;
		StringBuilder whereClause = new StringBuilder();

		// Build up the part of the query used to select fields.
		getFieldsToSelect(fieldsToSelect, fieldSelect);
		// Build up the where clause.
		buildWhereClause(extraWhereClauses, whereClause);

		if (length == 0) {
			// This represents a "select all", so we don't have to build up
			// much in the way of SQL. User sent {} (findAll at JSONStore layer)

			int limitInt = getLimit(limit);

			sql = buildFindAllQuery(limit, offset, sortString, fieldSelect, whereClause, limitInt);
		}

		else { //We got a query object (e.g. {fn: 'carlos'})
			sql = buildFindQuery(queryObj, limit, offset, exact, sortString, fieldSelect, selectionArgs, whereClause);
		}

		String[] str = new String[selectionArgs.size()];

		return rawQuery(sql, selectionArgs.toArray(str));
	}

	public Cursor findUsingQueryObject(JSONObject queryObj, String fieldsToSelect[], String extraWhereClauses[], String limit, String offset, Boolean exact) throws JSONException {
		return findUsingQueryObject(queryObj, fieldsToSelect, extraWhereClauses, limit, offset, exact, null);
	}

	private String buildFindQuery(JSONObject queryObj, String limit, String offset, Boolean exact, String sortString, StringBuilder fieldSelect, ArrayList<String> selectionArgs, StringBuilder whereClause) throws JSONException {
		String sql;
		StringBuilder clause = new StringBuilder();
		Iterator<?> keys = queryObj.keys();

		int limitInt = getLimit(limit);

		// Iterate over all the keys and build up both an SQL query and
		// the selection arguments array.
		handleExact(queryObj, exact, selectionArgs, clause, keys);

		if (whereClause.length() > 0) {
			clause.append(ReadableDatabase.SQL_AND);
			clause.append(whereClause);
		}

		if (limit == null) {
			sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE, fieldSelect.toString(), this.schema.getName(), clause.toString());

			if (hasSortParameter(sortString)) {
				//Remove the last semicolon, and add the sorting statement
				sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + sortString + ";"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {

			//Check if we need to add an offset
			if (offset != null) {
				//Handle case when results are being sorted:
				if (hasSortParameter(sortString)) {
					sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET_AND_ORDER, fieldSelect.toString(), this.schema.getName(), clause.toString(), sortString, limit, offset);
				} else {
					sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET, fieldSelect.toString(), this.schema.getName(), clause.toString(), limit, offset);
				}
			} else if (limitInt < 0) {
				limitInt = Math.abs(limitInt);
				limit = String.valueOf(limitInt);

				sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE_WITH_NEGATIVE_LIMIT, fieldSelect.toString(), this.schema.getName(), clause.toString(), DatabaseConstants.FIELD_ID, limit);
			} else {
				//Handle case when results are being sorted:
				if (hasSortParameter(sortString)) {
					sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_ORDER, fieldSelect.toString(), this.schema.getName(), clause.toString(), sortString, limit);
				} else {
					sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT, fieldSelect.toString(), this.schema.getName(), clause.toString(), limit);
				}
			}
		}
		return sql;
	}

	private void handleExact(JSONObject queryObj, Boolean exact, ArrayList<String> selectionArgs, StringBuilder clause, Iterator<?> keys) throws JSONException {
		if (!exact) {
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Object value = queryObj.get(key);

				clause.append("[" + JSONStoreUtil.getDatabaseSafeSearchFieldName(key) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				clause.append(ReadableDatabase.SQL_LIKE);

				if (keys.hasNext()) {
					clause.append(ReadableDatabase.SQL_AND);
				}

				if (value instanceof Boolean) {
					value = (Boolean) value ? 1 : 0;
				}

				selectionArgs.add("%" + value + "%"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			while (keys.hasNext()) {
				String key = (String) keys.next();//fn

				Object value = queryObj.get(key);
				String safeKey = "[" + JSONStoreUtil.getDatabaseSafeSearchFieldName(key) + "]"; //$NON-NLS-1$ //$NON-NLS-2$

				if (value instanceof Boolean) {
					value = (Boolean) value ? 1 : 0;
				}

				clause.append(" ( "); //$NON-NLS-1$
				clause.append(safeKey);
				clause.append(ReadableDatabase.SQL_EQ); //carlos (exact)
				clause.append(ReadableDatabase.SQL_OR);
				clause.append(safeKey);
				clause.append(ReadableDatabase.SQL_LIKE);//-@-carlos (end)
				clause.append(ReadableDatabase.SQL_OR);
				clause.append(safeKey);
				clause.append(ReadableDatabase.SQL_LIKE);//-@-carlos-@- (middle)
				clause.append(ReadableDatabase.SQL_OR);
				clause.append(safeKey);
				clause.append(ReadableDatabase.SQL_LIKE);//carlos-@- (start)

				selectionArgs.add("" + value); //$NON-NLS-1$
				selectionArgs.add("%-@-" + value); //$NON-NLS-1$
				selectionArgs.add("%-@-" + value + "-@-%"); //$NON-NLS-1$ //$NON-NLS-2$
				selectionArgs.add(value + "-@-%"); //$NON-NLS-1$

				clause.append(" ) "); //$NON-NLS-1$

				if (keys.hasNext()) {
					clause.append(ReadableDatabase.SQL_AND);
				}

			}
		}
	}

	private String buildFindAllQuery(String limit, String offset, String sortString, StringBuilder fieldSelect, StringBuilder whereClause, int limitInt) {
		String sql;
		if (whereClause.length() > 0) {
			String whereTemplate = ""; //$NON-NLS-1$
			if (limit == null) {
				//no limit specified
				whereTemplate = ReadableDatabase.SQL_FIND_WHERE;
				sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString());

				if (hasSortParameter(sortString)) {
					//Remove the last semicolon, and add the sorting statement
					sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + sortString + ";"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {

				//use the limit field

				//Check if we need to add an offset
				if (offset != null) {
					//Handle Limit and Offset passed:

					//Handle case when results are being sorted:
					if (hasSortParameter(sortString)) {
						whereTemplate = ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET_AND_ORDER;
						sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString(), sortString, limit, offset);
					} else {
						whereTemplate = ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_OFFSET;
						sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString(), limit, offset);
					}
				}

				else if (limitInt < 0) {
					//Handle negative Limit
					limitInt = Math.abs(limitInt);
					limit = String.valueOf(limitInt);

					whereTemplate = ReadableDatabase.SQL_FIND_WHERE_WITH_NEGATIVE_LIMIT;
					sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString(), DatabaseConstants.FIELD_ID, limit);
				}

				else {
					//Handle positive Limit

					//Handle case when results are being sorted:
					if (hasSortParameter(sortString)) {
						whereTemplate = ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT_AND_ORDER;
						sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString(), sortString, limit);
					} else {
						whereTemplate = ReadableDatabase.SQL_FIND_WHERE_WITH_LIMIT;
						sql = JSONStoreUtil.formatString(whereTemplate, fieldSelect.toString(), this.schema.getName(), whereClause.toString(), limit);
					}
				}

			}

		}

		else {
			sql = JSONStoreUtil.formatString(ReadableDatabase.SQL_FIND, fieldSelect.toString(), this.schema.getName());

			if (hasSortParameter(sortString)) {
				//Remove the last semicolon, and add the sorting statement
				sql = sql.substring(0, sql.length() - 1) + " ORDER BY " + sortString + ";"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return sql;
	}

	private boolean hasSortParameter(String sortString) {
		return sortString != null && sortString.trim().length() > 0;
	}

	private int getLimit(String limit) {
		int limitInt;
		try {
			limitInt = Integer.parseInt(limit);
		} catch (NumberFormatException e) {
			limitInt = 0;
		}
		return limitInt;
	}

	private void getFieldsToSelect(String[] fieldsToSelect, StringBuilder fieldSelect) {
		if ((fieldsToSelect == null) || (fieldsToSelect.length == 0)) {
			// Treat a null or empty set of fields to select as "*".

			fieldSelect.append('*');
		}

		else {
			for (int i = 0; i < fieldsToSelect.length; ++i) {
				fieldSelect.append(fieldsToSelect[i]);

				if (i < (fieldsToSelect.length - 1)) {
					fieldSelect.append(", "); //$NON-NLS-1$
				}
			}
		}
	}

	private void buildWhereClause(String[] extraWhereClauses, StringBuilder whereClause) {
		if ((extraWhereClauses != null) && (extraWhereClauses.length > 0)) {
			for (int i = 0; i < extraWhereClauses.length; ++i) {
				whereClause.append(extraWhereClauses[i]);

				if (i < (extraWhereClauses.length - 1)) {
					whereClause.append(ReadableDatabase.SQL_AND);
				}
			}
		}
	}

	public Cursor rawQuery(String sql, String selectionArgs[]) {
		this.logger.logTrace("executing query on database \"" + this.schema.getName() + "\":");
		this.logger.logTrace("   " + sql);

		if (selectionArgs != null) {
			this.logger.logTrace("arguments:");

			for (String selectionArg : selectionArgs) {
				this.logger.logTrace("   " + selectionArg);
			}
		}

		return this.database.rawQuery(sql, selectionArgs);
	}
}
