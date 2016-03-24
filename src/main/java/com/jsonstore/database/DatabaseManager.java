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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jsonstore.security.SecurityManager;
import com.jsonstore.util.JSONStoreLogger;
import com.jsonstore.util.JSONStoreUtil;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;




public class DatabaseManager {

	private static final String SQL_SCHEMA = "PRAGMA table_info({0})"; //$NON-NLS-1$

	private static boolean initialized = false;
	private static final DatabaseManager instance = new DatabaseManager();
	private static final JSONStoreLogger logger = JSONStoreUtil.getDatabaseLogger();

	private HashMap<String, DatabaseAccessor> accessors;
	private Database<?> database;
	private static boolean encryption = false;
	private String databaseKey;
	private String dbPath;

	private DatabaseManager() {
		this.accessors = new HashMap<String, DatabaseAccessor>();
	}

	public static DatabaseManager getInstance() {
		return DatabaseManager.instance;
	}

	public DatabaseAccessor getDatabase(String name) throws Exception {
		DatabaseAccessor accessor = this.accessors.get(name);

		if (accessor == null) {
			throw new Exception("could not retrieve unprovisioned " + "database \"" + name + "\"");
		}

		return accessor;
	}

	public static void setEncryption(boolean encrypt){
		encryption = encrypt;
	}
	
	public DatabaseAccessor getDatabase() throws Exception {
		if (accessors == null || accessors.size() == 0) {
			throw new Exception("could not retrieve unprovisioned database");
		}

		//Get the first available accessor in the accessor map; all accessors are the same, but they are tied to specific collections
		Object accessorObj = this.accessors.values().toArray()[0];

		DatabaseAccessor accessor = (DatabaseAccessor) (accessorObj);

		return accessor;
	}

	public boolean isDatabaseOpen() {
		return (this.database != null);
	}

	public void clearDatabaseKey() {
		this.databaseKey = null;
	}

	public void clearDbPath() {
		this.dbPath = null;
	}

	public void setDatabaseKey(Context context, String password, String username) throws Exception {
		this.databaseKey = SecurityManager.getInstance(context).getDPK(password, username);
	}

	public void setDbPath(String username) {
		this.dbPath = username + DatabaseConstants.DB_PATH_EXT;
	}

	public String getDbPath() {
		return this.dbPath;
	}

	public synchronized boolean checkDatabaseAgainstSchema(Context context, String modelName, DatabaseSchema schema) {
		Cursor cursor;

		openDatabaseIfNecessary(context);

		// We need to get the database table's schema and compare it against
		// the provided schema.

		cursor = this.database.rawQuery(JSONStoreUtil.formatString(DatabaseManager.SQL_SCHEMA, modelName), null);

		if (cursor != null) {
			int numRows = cursor.getCount();
			TreeMap<String, String> tableSchema = null;

			if (numRows > 0) {
				tableSchema = new TreeMap<String, String>();

				for (int i = 0; i < numRows; ++i) {
					cursor.moveToNext();
					tableSchema.put(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("type"))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			cursor.close();

			if (tableSchema != null) {
				return !schema.equals(tableSchema);
			}
		}

		// The table must not exist, so the schema can't be in conflict.

		return false;
	}

	public synchronized void closeDatabase() {
		this.database.close();
		this.accessors.clear();
		this.database = null;
	}

	public synchronized int destroyDatabase(Context context) {

		int rc = 0;
		File dbBaseDir = context.getDatabasePath(DatabaseConstants.DB_SUB_DIR);

		if (dbBaseDir.exists() && dbBaseDir.isDirectory()) {
			String[] children = dbBaseDir.list();

			if (children != null) {

				for (String c : children) {
					if (c != null) {
						if (!new File(dbBaseDir, c).delete()) {
							rc = DatabaseConstants.RC_DESTROY_FILE_FAILED;
						}
					}
				}
			}
		}

		return rc;
	}

	public synchronized void destroyKeychain(Context context) {
		SecurityManager.getInstance(context).destroyKeychain();
	}

	public synchronized void destroyPreferences(Context context) {
		SharedPreferences sp = context.getSharedPreferences(DatabaseConstants.JSONSTORE_PREFS, android.content.Context.MODE_PRIVATE);
		if (sp != null) {
			SharedPreferences.Editor editor = sp.edit();
			editor.clear();
			editor.commit();
		}
	}

	private void openDatabaseIfNecessary(Context context) {


		if (this.database == null) {
			// The database is closed, so open it.

			if (this.databaseKey == null) {
				// Set the database key to an empty string to disable
				// encryption.

				this.databaseKey = ""; //$NON-NLS-1$
			}
			File dbFile = new File(context.getDatabasePath(DatabaseConstants.DB_SUB_DIR), dbPath);

			try {
				if(encryption){
					this.database = (Database<?>) Class.forName(DatabaseConstants.SQLCIPHER_CLASS)
							.getConstructor()
							.newInstance();
					this.database.openDatabase(dbFile.getAbsolutePath(), this.databaseKey, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY, context);
				} else {
					this.database = (Database<?>) Class.forName(DatabaseConstants.SQLITE_CLASS)
							.getConstructor()
							.newInstance();
					this.database.openDatabase(dbFile.getAbsolutePath(), SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
				}

			} catch(Exception e){
				e.printStackTrace();
			}



		}
	}

	public synchronized boolean provisionDatabase(Context context, DatabaseSchema schema, boolean dropFirst){

		boolean exists = false;
		String name = schema.getName();
		openDatabaseIfNecessary(context);
		DatabaseAccessor accessor = new DatabaseAccessorImpl(this.database, schema);

		synchronized (accessor) {
			if (dropFirst) {
				// Wipe out the database (or rather, the table for the model).
				accessor.dropTable();
			}

			if (accessor.getTableExists()) {
				exists = true;
			}

			logger.logDebug("provisioning database \"" + name + "\" (" + "already exists: " + exists + ")");

			if (!exists) {
				accessor.createTable();
			}

			this.accessors.put(name, accessor);
		}

		return exists;
	}
}