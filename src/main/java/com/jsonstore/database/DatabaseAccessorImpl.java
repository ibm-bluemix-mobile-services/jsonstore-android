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

import java.util.HashSet;
import java.util.Iterator;

public class DatabaseAccessorImpl implements DatabaseAccessor {
	private static final String SQL_CREATE_TABLE = "CREATE TABLE {0} ({1} INTEGER PRIMARY KEY AUTOINCREMENT, {2} {3} TEXT, {4} REAL DEFAULT 0, {5} INTEGER DEFAULT 0, {6} TEXT);"; //$NON-NLS-1$
	private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS {0};"; //$NON-NLS-1$
	private static final String SQL_TABLE_EXISTS = "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = \"{0}\";"; //$NON-NLS-1$

	private static final HashSet<String> ignoredSchemaNodes = new HashSet<String>();
	private static final JSONStoreLogger logger = JSONStoreUtil.getDatabaseLogger();

	static {
		// Set up the ignored schema nodes.  We don't want to use these in
		// formatSchemaColumns() because we have our own definitions for
		// them in the create SQL.

		DatabaseAccessorImpl.ignoredSchemaNodes.add(DatabaseConstants.FIELD_DELETED);
		DatabaseAccessorImpl.ignoredSchemaNodes.add(DatabaseConstants.FIELD_DIRTY);
		DatabaseAccessorImpl.ignoredSchemaNodes.add(DatabaseConstants.FIELD_ID);
		DatabaseAccessorImpl.ignoredSchemaNodes.add(DatabaseConstants.FIELD_JSON);
		DatabaseAccessorImpl.ignoredSchemaNodes.add(DatabaseConstants.FIELD_OPERATION);
	}

	private SQLiteDatabase database;
	private ReadableDatabase readableDB;
	private DatabaseSchema schema;
	private WritableDatabase writableDB;

	protected DatabaseAccessorImpl(SQLiteDatabase database, DatabaseSchema schema) {
		this.database = database;
		this.readableDB = new ReadableDatabase(database, schema);
		this.schema = schema;
		this.writableDB = new WritableDatabase(database, schema);
	}

	@Override
	public SQLiteDatabase getRawDatabase() {
		return this.database;
	}

	@Override
	public ReadableDatabase getReadableDatabase() {
		return this.readableDB;
	}

	@Override
	public DatabaseSchema getSchema() {
		return this.schema;
	}

	public boolean getTableExists() {
		Cursor cursor;
		boolean result = false;
		String sql = JSONStoreUtil.formatString(DatabaseAccessorImpl.SQL_TABLE_EXISTS, this.schema.getName());

		cursor = this.readableDB.rawQuery(sql, null);

		if (cursor != null) {
			if (cursor.getCount() > 0) {
				result = true;
			}

			cursor.close();
		}

		return result;
	}

	@Override
	public WritableDatabase getWritableDatabase() {
		return this.writableDB;
	}

	public void createTable() {
		String name = this.schema.getName();

		logger.logTrace("creating database \"" + name + "\"");

		execSQL(DatabaseAccessorImpl.SQL_CREATE_TABLE, name, DatabaseConstants.FIELD_ID, formatSchemaColumns(), DatabaseConstants.FIELD_JSON, DatabaseConstants.FIELD_DIRTY, DatabaseConstants.FIELD_DELETED, DatabaseConstants.FIELD_OPERATION);
	}

	public void dropTable() {
		String name = this.schema.getName();

		logger.logTrace("[!!!] dropping database \"" + name + "\"");

		execSQL(DatabaseAccessorImpl.SQL_DROP_TABLE, name);
	}

	private void execSQL(String sql, Object... args) {
		String formattedSQL = JSONStoreUtil.formatString(sql, args);

		logger.logTrace("executing SQL on database \"" + this.schema.getName() + "\":");
		logger.logTrace("   " + formattedSQL);

		this.writableDB.getDatabase().execSQL(formattedSQL);
	}

	private String formatSchemaColumns() {
		StringBuilder result = new StringBuilder();
		Iterator<String> schemaNodeNames = this.schema.getSearchFieldIterator();
		HashSet<String> schemaNodes = new HashSet<String>();

		// Iterate over all the schema nodes and remove the ones we're
		// ignoring (see note in the static initializer).

		while (schemaNodeNames.hasNext()) {
			String schemaNodeName = schemaNodeNames.next();

			if (!DatabaseAccessorImpl.ignoredSchemaNodes.contains(schemaNodeName)) {
				schemaNodes.add(schemaNodeName);
			}
		}

		// Finally, create a string of properly-formatted schema node
		// definitions.

		for (String schemaNode : schemaNodes) {
			result.append("'" + JSONStoreUtil.getDatabaseSafeSearchFieldName(schemaNode) + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			result.append(' ');
			result.append(this.schema.getSearchFieldType(schemaNode).getMappedType());
			result.append(", "); //$NON-NLS-1$
		}

		return result.toString();
	}
}
