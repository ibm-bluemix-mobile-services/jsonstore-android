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

import android.content.Context;
import android.content.SharedPreferences;

import com.jsonstore.database.DatabaseAccessor;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.DatabaseManager;
import com.jsonstore.database.DatabaseSchema;
import com.jsonstore.exceptions.JSONStoreCloseAllException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreDestroyFailureException;
import com.jsonstore.exceptions.JSONStoreDestroyFileError;
import com.jsonstore.exceptions.JSONStoreFileAccessException;
import com.jsonstore.exceptions.JSONStoreInvalidPasswordException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.exceptions.JSONStoreMetadataRemovalFailure;
import com.jsonstore.exceptions.JSONStoreMigrationException;
import com.jsonstore.exceptions.JSONStoreNoTransactionInProgressException;
import com.jsonstore.exceptions.JSONStoreSchemaMismatchException;
import com.jsonstore.exceptions.JSONStoreTransactionDuringInitException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.exceptions.JSONStoreTransactionInProgressException;
import com.jsonstore.util.JSONStoreUtil;

import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class JSONStore {
    private Context context = null;
    private Logger logger = JSONStoreUtil.getCoreLogger();
    private static boolean transactionInProgress = false;
    private String username;

    private static JSONStore instance;
    private Map<String, JSONStoreCollection> collectionMap = new HashMap<String, JSONStoreCollection>();

    /**
     * @exclude
     */
    private JSONStore(Context context) {
        this.context = context;
    }

    /**
     * @exclude
     */
    private Context getContext() {
        return context;
    }

    /**
     * @exclude
     */
    String getUsername() {
        return this.username;
    }

    /**
     * @exclude
     */
    private void checkVersionMigration(android.content.Context context) throws JSONStoreFileAccessException, JSONStoreMigrationException {
        //JSONStoreLogger.logFileInfo(getFileInfo());

        SharedPreferences sp = context.getSharedPreferences(DatabaseConstants.JSONSTORE_PREFS, android.content.Context.MODE_PRIVATE);
        String ver = sp.getString(DatabaseConstants.JSONSTORE_VERSION_PREF, null);

        // We don't have a version key, which means we need to migrate.
        if (ver == null) {
            logger.trace("Performing migation to JSONStore 2.0");

            // Check if the com.worklight.jsonstore directory exists, if not create it.
            File dbBaseDir = context.getDatabasePath(DatabaseConstants.DB_SUB_DIR);
            if (!dbBaseDir.exists()) {

                boolean mkdirWorked = dbBaseDir.mkdirs();
                if (!mkdirWorked) {
                    String message = "Unable to create com.worklight.jsonstore directory.";
                    JSONStoreFileAccessException e = new JSONStoreFileAccessException(message);
                    logger.trace(message);
                    throw e;
                }
            }

            // Check if old DB file exists, if so move it to the com.worklight.jsonstore directory.
            File dbFile = context.getDatabasePath(DatabaseConstants.OLD_DB_PATH);
            if (dbFile.exists()) {

                boolean moveWorked = dbFile.renameTo(new File(dbBaseDir, DatabaseConstants.DEFAULT_USERNAME + DatabaseConstants.DB_PATH_EXT));
                if (moveWorked) {
                    logger.trace("Migration to JOSNStore 2.0 successful.");
                } else {
                    String message = "Unable to migrate existing JSONStore database to version 2.0";
                    JSONStoreMigrationException e = new JSONStoreMigrationException(message);
                    logger.trace(message);
                    throw e;
                }
            }

            // Save the version preference.
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(DatabaseConstants.JSONSTORE_VERSION_PREF, "2.0"); //$NON-NLS-1$
            editor.commit();
        }
    }

    /**
     * @exclude
     */
    private boolean provisionDatabase(JSONStoreCollection collection, DatabaseSchema schema, String username, boolean dropFirst) throws JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException {

        // Check if the store has to be migrated:
        checkVersionMigration(getContext());

        DatabaseManager dbManager = DatabaseManager.getInstance();

        handleUsername(dbManager, username);

        if (!dropFirst && schema.isSchemaMismatched(collection.getName(), schema, getContext())) {
            // The database already exists and we're requesting to re-provision it with a different schema,
            // so return an error.

            String message = "Table schema mismatch for existing collection.";
            JSONStoreSchemaMismatchException jsException = new JSONStoreSchemaMismatchException(message);
            logger.trace(message);
            throw jsException;
        }

        if (!dbManager.provisionDatabase(getContext(), schema, dropFirst)) {
            // The table doesn't exist, and we need to get a handle to
            // it in order to populate everything and save it to disk.

            try {
                dbManager.getDatabase(collection.getName()).getReadableDatabase();
            } catch (Exception e) {
                String message = "Could not retreive a database accessor.";
                JSONStoreFileAccessException jsException = new JSONStoreFileAccessException(message, e);
                logger.trace(message);
                throw jsException;
            }

            return false;
        } else {
            return true;
        }

    }

    /**
     * @exclude
     */
    private void handleUsername(DatabaseManager dbManager, String username) throws JSONStoreCloseAllException, JSONStoreInvalidPasswordException {

        if (username == null) {
            if (dbManager.getDbPath() == null) {
                // assume default user name.
                dbManager.setDbPath(DatabaseConstants.DEFAULT_USERNAME + DatabaseConstants.DB_PATH_EXT);
            }

            if (!dbManager.getDbPath().equalsIgnoreCase(DatabaseConstants.DEFAULT_USERNAME + DatabaseConstants.DB_PATH_EXT)) {
                // you used a user name that does not match the current logged user.
                String message = "You tried to login with a user that is not the default user that is currently logged in. Call closeAll first.";
                JSONStoreCloseAllException jsException = new JSONStoreCloseAllException(message);
                logger.trace(message);
                throw jsException;
            }
        }

        else { // user name not null.
            if (dbManager.getDbPath() == null) {
                // No user logged in, set the current logged user.
                dbManager.setDbPath(username);

            } else if (!dbManager.getDbPath().equals(username + DatabaseConstants.DB_PATH_EXT)) {
                // you used a user name that does not match the current logged user.
                String message = "You tried to login with a user that is not " + dbManager.getDbPath() + ". Call closeAll first.";
                JSONStoreCloseAllException jsException = new JSONStoreCloseAllException(message);
                logger.trace(message);
                throw jsException;

            }
        }
    }

    /**
     * @exclude
     */
    private void disownAllCollections(String username) {
        for (JSONStoreCollection col : collectionMap.values()) {
            col.disown(username);
        }

        collectionMap.clear();
    }

    /**
     * @exclude
     */
    private void disownAllCollections() {
        disownAllCollections(null);
    }

    /**
     * @exclude
     */
    void removeCollectionReference(JSONStoreCollection col) {
        col.disown();
        collectionMap.remove(col);
    }

    /**
     * Responsible for retrieving a WLJSONStore instance that is unique to the application's Android context object.
     *
     * @param android_context
     *            The current Android context object that is associated with this application.
     * @return A WLJSONStore object that can be used to initialize and manipulate collections.
     */
    public static JSONStore getInstance(Context android_context) {
        if (instance == null) {
            instance = new JSONStore(android_context);
        }

        return instance;
    }

    /**
     * Provides access to the collections that are inside the store, and creates them if they do not already exist.
     *
     * @param collections
     *            An array of collection definitions (JSONStoreCollection objects) that are to be initialized (either reopened or
     *            created).
     * @param initOptions
     *            Specific set of options to initialize the collection with (such as security credentials).
     * @throws JSONStoreInvalidSchemaException
     * @throws JSONStoreSchemaMismatchException
     * @throws JSONStoreInvalidPasswordException
     * @throws JSONStoreCloseAllException
     * @throws JSONStoreMigrationException
     * @throws JSONStoreFileAccessException
     * @throws JSONStoreTransactionDuringInitException
     * @throws Exception
     */
    public void openCollections(List<JSONStoreCollection> collections, JSONStoreInitOptions initOptions) throws JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONStoreTransactionDuringInitException {
        if (transactionInProgress) {
            throw new JSONStoreTransactionDuringInitException("Cannot open collections while executing a transaction.");
        }

        if (collections == null)
            return;

        boolean dropFirst = false;

        // Get options, if specified.
        if (initOptions == null) {
            initOptions = new JSONStoreInitOptions();
        }

        dropFirst = initOptions.isClear();
        username = initOptions.getUsername();


        for (JSONStoreCollection collection : collections) {
            // Parse the provided schema.
            DatabaseSchema schema = null;

            if (collection == null) {
                continue; // Move to the next collection. This one is null.
            }

            try {
                schema = new DatabaseSchema(collection.getName(), collection.getAllSearchFields());
            }

            catch (Throwable e) {
                // Invalid schema provided, so return an error.
                String message = "Error when validating schema.";
                JSONStoreInvalidSchemaException jsException = new JSONStoreInvalidSchemaException(message, e);
                logger.trace(message);
                throw jsException;
            }

            // Provision the database.
            boolean wasReopened = provisionDatabase(collection, schema, username, dropFirst);
            collectionMap.put(collection.getName(), collection);
            collection.initialize(this, schema, wasReopened);

            //logInst.end();
        }
    }

    /**
     * Provides access to the collections that are inside the store, and creates them if they do not already exist.
     *
     * @param collections
     *            An array of collection definitions (JSONStoreCollection objects) that are to be initialized (either reopened or
     *            created).
     * @throws JSONStoreSchemaMismatchException
     * @throws JSONStoreInvalidPasswordException
     * @throws JSONStoreCloseAllException
     * @throws JSONStoreMigrationException
     * @throws JSONStoreFileAccessException
     * @throws JSONStoreInvalidSchemaException
     * @throws JSONStoreTransactionDuringInitException
     * @throws Exception
     */
    public void openCollections(List<JSONStoreCollection> collections) throws JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONStoreTransactionDuringInitException {
        openCollections(collections, null);
    }

    /**
     * Locks access to all the collections until the init method is called.
     *
     * @throws JSONStoreCloseAllException
     *             An unexpected error occurred when trying to close access to all collections.
     * @throws JSONStoreDatabaseClosedException
     *             The database is already closed, so this operation is futile.
     * @throws JSONStoreTransactionFailureException
     */
    public void closeAll() throws JSONStoreCloseAllException, JSONStoreDatabaseClosedException, JSONStoreTransactionFailureException {
        try {
            if (transactionInProgress) {
                throw new JSONStoreTransactionFailureException("Cannot close collections while executing a transaction.");
            }

            DatabaseManager dbManager = DatabaseManager.getInstance();

            // Make sure the database is actually open first.
            if (!dbManager.isDatabaseOpen()) {
                String message = "Could not close all collections. The database is not open.";
                JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
                logger.trace(message);
                throw jsException;
            }

            try {
                dbManager.clearDbPath();
                dbManager.clearDatabaseKey();
                dbManager.closeDatabase();

                disownAllCollections();
            } catch (Throwable e) {
                String message = "Could not close the database. An exception occurred.";
                JSONStoreCloseAllException jsException = new JSONStoreCloseAllException(message, e);
                logger.trace(message);
                throw jsException;
            }
        } finally {
            //logInst.end();
        }

    }

    /**
     * Provides an accessor to the collection if the collection exists. This method depends on the init method being called first,
     * with the requested collection name.
     *
     * @param collectionName
     *            The name of the initialized JSONStore collection to retrieve.
     * @return A JSONStoreCollection object of an already initialized collection. Returns null if a collection by that name does
     *         not exist.
     */
    public JSONStoreCollection getCollectionByName(String collectionName) {
        JSONStoreCollection col = collectionMap.get(collectionName);

        // If we already know it doesn't exist, return null.
        if (col == null)
            return null;

        // Check to see if it has been removed. If so, remove it from our map then return null.
        try {
            DatabaseManager.getInstance().getDatabase(collectionName);
        } catch (Exception e) {
            removeCollectionReference(col);
            return null;
        }

        // If it's still open, return it.
        return col;
    }

    /**
     * Permanently deletes all data, clears security artifacts, and removes the accessor for a specific user.
     *
     * @throws JSONStoreDestroyFailureException
     * 	Unexpected failure.
     * @throws JSONStoreTransactionFailureException
     * 	Transaction in progress.
     * @throws JSONStoreDestroyFileError
     * 	Failure to remove the file.
     * @throws JSONStoreMetadataRemovalFailure
     * 	Failure to remove metadata.
     */
    public void destroy(String username) throws JSONStoreTransactionFailureException, JSONStoreDestroyFailureException, JSONStoreDestroyFileError, JSONStoreMetadataRemovalFailure {
        try {
            if (transactionInProgress) {
                throw new JSONStoreTransactionFailureException("Cannot destroy store while executing a transaction.");
            }

            DatabaseManager dbManager = DatabaseManager.getInstance();

            if (dbManager.isDatabaseOpen()) {
                dbManager.closeDatabase();
            }

            dbManager.clearDbPath();
            dbManager.clearDatabaseKey();

            String dpkKey = "dpk-" + username;

            SharedPreferences sp = this.context.getSharedPreferences ("dpkPrefs", Context.MODE_PRIVATE);

            sp.edit().remove(dpkKey).commit();

            String dpkAfterRemove = sp.getString(dpkKey, null);

            if (dpkAfterRemove != null) {

                // DPK still exists, worked = NO
                String message = "Failed to remove the following DPK Key: "+ dpkKey + " with content: " + dpkAfterRemove;
                JSONStoreMetadataRemovalFailure jsException = new JSONStoreMetadataRemovalFailure(message);
                logger.trace(message);
                throw jsException;

            } else {

                String dbBaseDir = this.context.getDatabasePath("wljsonstore").getPath();

                File userStoreFile = new File(dbBaseDir + File.separator + username + ".sqlite");

                if (userStoreFile.exists()) {

                    if (! userStoreFile.delete()) {

                        //Failed to remove file, worked = NO
                        String message = "Failed to remove the following file: " + userStoreFile;
                        JSONStoreDestroyFileError jsException = new JSONStoreDestroyFileError(message);
                        logger.trace(message);
                        throw jsException;

                    } else {

                        //File and shared preference item removed successfully
                        //worked = YES
                    }

                } else {

                    //There is nothing to remove, returning success
                    //worked = YES
                }
            }

            //Clean up
            transactionInProgress = false;
            disownAllCollections(username);

        } catch (Throwable t){

            throw new JSONStoreDestroyFailureException(t.getMessage());

        } finally {

         //   logInst.end();
        }
    }

    /**
     * Permanently deletes all data for all users, clears security artifacts, and removes accessors.
     *
     * @throws JSONStoreDestroyFailureException
     *             When a system failure occurs (such as lack of file permissions). The destroy has failed completely.
     * @throws JSONStoreTransactionFailureException
     */
    public void destroy() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
        try {
            if (transactionInProgress) {
                throw new JSONStoreTransactionFailureException("Cannot destroy store while executing a transaction.");
            }

            DatabaseManager dbManager = DatabaseManager.getInstance();
            int result;

            // Destroy the keychain.
            dbManager.destroyPreferences(getContext());
            dbManager.clearDbPath();
            dbManager.clearDatabaseKey();

            // Close the database if it's already open.
            if (dbManager.isDatabaseOpen()) {
                dbManager.closeDatabase();
            }

            disownAllCollections();

            transactionInProgress = false;

            result = dbManager.destroyDatabase(getContext());

            if (result != 0) {
                // The database file and the keychain were not destroyed, so throw exception:
                String message = "There was an error when destroying the JSONStore. The destroyDatabase failed.";
                JSONStoreDestroyFailureException jsException = new JSONStoreDestroyFailureException(message);
                logger.trace(message);
                throw jsException;

            }
        } finally {
            //logInst.end();
        }
    }

    /**
     * Returns a list of objects with information about all the stores in the device.
     * It contains the name of the store, the size, and whether they are encrypted or not.
     *
     * @return A JSONStoreFileInfo list that contains the file information.
     */
    public List<JSONStoreFileInfo> getFileInfo() {
        Map<String, JSONStoreFileInfo> results = new TreeMap<String, JSONStoreFileInfo>();
        File dbBaseDir = context.getDatabasePath(DatabaseConstants.DB_SUB_DIR);
        FileInputStream in = null;
        if(dbBaseDir.exists() && dbBaseDir.isDirectory()) {

            File[] subFiles = dbBaseDir.listFiles();

            for(File possibleDB : subFiles) {
                if(possibleDB != null && possibleDB.isFile() && possibleDB.getName().endsWith(DatabaseConstants.DB_PATH_EXT)) {
                    //This is an sqlite file. Username is the name of the file
                    String name = possibleDB.getName();
                    String username = name.substring(0, name.length() - DatabaseConstants.DB_PATH_EXT.length());
                    long fileSizeBytes = possibleDB.length();

                    try {
                        //Following http://sqlite.org/fileformat.html, first 16 bytes should contain 'sqlite'
                        in = new FileInputStream(possibleDB);  //TODO: close stream!
                        byte[] first6Bytes = new byte[6];
                        in.read(first6Bytes, 0, 6);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if(in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }


                    JSONStoreFileInfo currentDB = new JSONStoreFileInfo(username, fileSizeBytes);
                    results.put(username, currentDB);

                }
            }
        }


        return new ArrayList<JSONStoreFileInfo>(results.values());
    }


    /**
     * Begin a new transaction. After starting the transaction, the following methods will not work until the transaction is
     * committed or rolled back: open, destroy, closeAll, and removeCollection.
     *
     * @throws JSONStoreTransactionInProgressException
     *             occurs if a transaction is already in progress.
     * @throws JSONStoreTransactionFailureException
     *             occurs if there was an error while starting the transaction.
     * @throws JSONStoreDatabaseClosedException
     *             occurs if the collection has not been opened.
     */
    public synchronized void startTransaction() throws JSONStoreTransactionInProgressException, JSONStoreTransactionFailureException, JSONStoreDatabaseClosedException {
        try {
            DatabaseAccessor acc = null;

            try {
                acc = DatabaseManager.getInstance().getDatabase();
            } catch (Exception e) {
                throw new JSONStoreDatabaseClosedException();
            }

            if (isTransactionInProgress()) {
                throw new JSONStoreTransactionInProgressException("Cannot start a new transaction; a transaction is already in progress.");
            }

            try {
                acc.getRawDatabase().beginTransaction();
                transactionInProgress = true;
            } catch (Throwable e) {
                throw new JSONStoreTransactionFailureException(e);
            }
        } finally {
            //logInst.end();
        }
    }

    /**
     * @exclude
     *
     *          Check if there is a transaction in progress.
     * @return true if a transaction is in progress
     */
    public synchronized boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    /**
     * Commit a transaction.
     *
     * @return true if the transaction finished successfully.
     * @throws JSONStoreNoTransactionInProgressException
     *             occurs if there is no transaction that is currently in progress.
     * @throws JSONStoreTransactionFailureException
     *             occurs if there is a problem while committing the transaction.
     * @throws JSONStoreDatabaseClosedException
     *             occurs if the collection has not been opened.
     */
    public synchronized boolean commitTransaction() throws JSONStoreNoTransactionInProgressException, JSONStoreTransactionFailureException, JSONStoreDatabaseClosedException {
        try {
            DatabaseAccessor acc = null;

            try {
                acc = DatabaseManager.getInstance().getDatabase();
            } catch (Exception e) {
                throw new JSONStoreDatabaseClosedException();
            }

            if (!isTransactionInProgress()) {
                throw new JSONStoreNoTransactionInProgressException("No transaction in progress; cannot commit transaction.");
            }

            try {
                acc.getRawDatabase().setTransactionSuccessful();
                acc.getRawDatabase().endTransaction();
                transactionInProgress = false;
            } catch (Throwable e) {
                throw new JSONStoreTransactionFailureException(e);
            }

            return true;
        } finally {
            //logInst.end();
        }
    }

    /**
     * Roll back a transaction.
     *
     * @return true if the transaction rollback was successful.
     * @throws JSONStoreNoTransactionInProgressException
     *             occurs if there is no transaction that is currently in progress.
     * @throws JSONStoreDatabaseClosedException
     *             occurs if the collection has not been opened.
     * @throws JSONStoreTransactionFailureException
     *             occurs if there is a problem while rolling back the transaction.
     */
    public synchronized boolean rollbackTransaction() throws JSONStoreNoTransactionInProgressException, JSONStoreDatabaseClosedException, JSONStoreTransactionFailureException {
        try {
            DatabaseAccessor acc = null;

            try {
                acc = DatabaseManager.getInstance().getDatabase();
            } catch (Exception e) {
                throw new JSONStoreDatabaseClosedException();
            }

            if (!isTransactionInProgress()) {
                throw new JSONStoreNoTransactionInProgressException("No transaction in progress; cannot roll back transaction.");
            }

            try {
                acc.getRawDatabase().endTransaction();
                transactionInProgress = false;
            } catch (Throwable e) {
                throw new JSONStoreTransactionFailureException(e);
            }

            return true;
        } finally {
            //logInst.end();
        }
    }

}
