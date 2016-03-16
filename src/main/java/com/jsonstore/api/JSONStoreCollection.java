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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jsonstore.database.DatabaseAccessor;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.DatabaseManager;
import com.jsonstore.database.DatabaseSchema;
import com.jsonstore.database.QueryBuilder;
import com.jsonstore.database.QueryBuilderSelect;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.database.WritableDatabase;
import com.jsonstore.exceptions.JSONStoreAddException;
import com.jsonstore.exceptions.JSONStoreChangeException;
import com.jsonstore.exceptions.JSONStoreCountException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreDirtyCheckException;
import com.jsonstore.exceptions.JSONStoreException;
import com.jsonstore.exceptions.JSONStoreFilterException;
import com.jsonstore.exceptions.JSONStoreFindException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.exceptions.JSONStoreMarkCleanException;
import com.jsonstore.exceptions.JSONStoreRemoveCollectionException;
import com.jsonstore.exceptions.JSONStoreRemoveException;
import com.jsonstore.exceptions.JSONStoreReplaceException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.jackson.JacksonSerializedJSONObject;
import com.jsonstore.jackson.JsonOrgModule;
import com.jsonstore.util.JSONStoreLogger;
import com.jsonstore.util.JSONStoreLogger.JSONStoreAnalyticsLogInstance;
import com.jsonstore.util.JSONStoreUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a single JSONStore collection. Operations on the collection can be done by using the API for this class.
 *
 */
public class JSONStoreCollection {
    private static final int FIND_BY_ID_CHUNK_SIZE = 200;
    private String name;
    private Map<String, SearchFieldType> searchFields, additionalSearchFields;
    private JSONStore initializedJSONStoreInstance;
    private boolean wasReopened;
    private DatabaseSchema schema;
    private JSONStoreLogger logger = JSONStoreUtil.getCoreLogger();

    public JSONStoreCollection(String name) throws JSONStoreInvalidSchemaException {

        this.searchFields = new HashMap<String, SearchFieldType>();
        this.additionalSearchFields = new HashMap<String, SearchFieldType>();

        if(name == null || name.isEmpty()) {
            String message = "Error when creating the collection. Collection name cannot be null.";
            JSONStoreInvalidSchemaException jsException = new JSONStoreInvalidSchemaException(message);
            logger.logError(message, jsException);
            throw jsException;
        }
        this.name = name;
    }

    /**
     * @exclude
     */
    private Cursor runQuery(QueryBuilder selectQuery) throws JSONStoreDatabaseClosedException {
        if (selectQuery == null) return null;

        DatabaseAccessor acc = getAccessor();
        StringBuilder rawQueryString = new StringBuilder();
        List<String> rawQueryParams = new LinkedList<String>();
        selectQuery.convertToQueryString(rawQueryString, rawQueryParams);
        String[] rawQueryParamsArray = (String[]) rawQueryParams.toArray(new String[rawQueryParams.size()]);

        return acc.getRawDatabase().rawQuery(rawQueryString.toString(), rawQueryParamsArray);
    }

    /**
     * @exclude
     */
    private DatabaseAccessor getAccessor() throws JSONStoreDatabaseClosedException {

        // The store instance is null, so we know we are closed. Give up early.
        if (this.initializedJSONStoreInstance == null) {
            String message = "Collection is not initialized.";
            JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
            logger.logError(message, jsException);
            throw jsException;
        }

        DatabaseManager mgr = DatabaseManager.getInstance();
        if (mgr == null || !mgr.isDatabaseOpen()) {
            String message = "Database manager is null or database not opened.";
            JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
            logger.logError(message, jsException);
            throw jsException;
        }

        DatabaseAccessor acc = null;
        try {
            acc = mgr.getDatabase(getName());
        } catch (Exception e) {
            String message = "Could not get database accessor. Database is not open.";
            JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
            logger.logError(message, jsException);
            throw jsException;
        }

        if (acc == null) {
            String message = "Database accessor is not open. The database is not open.";
            JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
            logger.logError(message, jsException);
            throw jsException;
        }

        SQLiteDatabase rawDB = acc.getRawDatabase();
        if (rawDB == null || !rawDB.isOpen()) {
            String message = "Could not get raw collection instance. The database is not open.";
            JSONStoreDatabaseClosedException jsException = new JSONStoreDatabaseClosedException(message);
            logger.logError(message, jsException);
            throw jsException;
        }

        return acc;
    }

    /**
     * @exclude
     */
    private boolean isJSONCreatedColumn(String column) {

        if(column.equals(DatabaseConstants.FIELD_DELETED) || column.equals(DatabaseConstants.FIELD_DIRTY)
                || column.equals(DatabaseConstants.FIELD_ID) || column.equals(DatabaseConstants.FIELD_JSON) ||
                column.equals(DatabaseConstants.FIELD_OPERATION)){

            return true;
        }

        return false;
    }

    /**
     * @exclude
     */
    private List<JSONObject> removeFilterDuplicates(List<JSONObject> list){

        List<JSONObject> result = new ArrayList<JSONObject>();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();

        for(JSONObject obj : list){
            if(!map.containsKey(obj.toString())){
                map.put(obj.toString(), true);
                result.add(obj);
            }
        }

        return result;
    }

    /**
     * @exclude
     */
    private void addNonDuplicates(LinkedHashMap<Integer, JSONObject> resultHash, List<JSONObject> results) throws JSONStoreFindException {
        try{
            if (results != null) {
                //JSONObject is awful and apparently equals doesn't work, so we have to track dupes ourself.
                for (JSONObject jso : results) {
                    Integer id = jso.getInt (DatabaseConstants.FIELD_ID);
                    if(! resultHash.containsKey (id)){
                        resultHash.put (id, jso);
                    }

                }
            }
        }
        catch (JSONException e) {
            String message = "Error when attempting to find a document. A JSONException occurred.";
            JSONStoreFindException jsException = new JSONStoreFindException(message, e);
            logger.logError(message, jsException);
            throw jsException;
        }
    }

    /**
     * @exclude Called by WLJSONStore internally (package private) to signal to the
     *          collection that this collection object is recognized as opened,
     *          providing scope details.
     */
    void initialize(JSONStore instance, DatabaseSchema schema, boolean reopened) {
        this.initializedJSONStoreInstance = instance;
        this.schema = schema;
        this.wasReopened = reopened;
    }

    /**
     * @exclude Same as disown without the parameter. This is used by destroy with username to
     * ensure the collection is not recognized by WLJSONStore as active.
     */
    void disown(String username) {

        if (username == null || this.initializedJSONStoreInstance.getUsername().equalsIgnoreCase(username)) {
            this.initializedJSONStoreInstance = null;
        }
    }

    /**
     * @exclude Called by WLJSONStore internally (package private) to signal to the
     *          collection that is this collection object is no longer recognized by
     *          the store. Possible reason: the collection is closed or destroyed.
     */
    void disown() {

        disown(null);
    }

    private String getUsername() {
        if(initializedJSONStoreInstance != null){
            return initializedJSONStoreInstance.getUsername();
        }

        return ""; //$NON-NLS-1$
    }

    /**
     * Get the name of this collection.
     * @return The name of the collection.
     */
    public String getName() {
        return name;
    }

    /**
     * Determine if the collection was reopened when it was initialized.
     * @return True if the collection was reopened (already existed) when the collection
     *         is opened, or false if it is a brand new collection.
     */
    public boolean wasReopened() {
        return wasReopened;
    }

    /**
     * Return a map of search fields from the collection.
     *
     * @return A list of search fields that are defined in the collection.
     */
    public Map<String, SearchFieldType> getSearchFields() {
        return searchFields;
    }

    /**
     * Set a search field for the collection.
     *
     * @param key
     *            The name of the search field to add. Only useful before the collection
     *            is opened.
     * @param type
     *            The type of the search field.
     * @see SearchFieldType
     */
    public void setSearchField(String key, SearchFieldType type) {
        if(key == null || key.isEmpty()) return;
        type = (type != null) ? type : SearchFieldType.STRING;
        searchFields.put(key, type);
    }

    /**
     * Determine if a collection contains the given search field.
     *
     * @param search_field
     *            The name of the search field to look up.
     * @return True if the search field is defined in the collection, false if
     *         it is not defined.
     */
    public boolean hasSearchField(String search_field) {
        if (search_field.equals(DatabaseConstants.FIELD_DELETED) || search_field.equals(DatabaseConstants.FIELD_ID) || search_field.equals(DatabaseConstants.FIELD_DIRTY) || search_field.equals(DatabaseConstants.FIELD_OPERATION)) {
            return true;
        }
        return searchFields.containsKey(search_field);
    }

    /**
     * Set an additional search field for the collection.
     *
     * @param key
     *            The name of the additional search field to add. Only useful before
     *            the collection is opened.
     * @param type
     *            The type of the search field.
     * @see SearchFieldType
     */
    public void setAdditionalSearchField(String key, SearchFieldType type) {
        type = (type != null) ? type : SearchFieldType.STRING;
        additionalSearchFields.put(key, type);
    }

    /**
     * Get a map of the additional search fields for this collection.
     * @return A map of additional search fields.
     */
    public Map<String, SearchFieldType> getAdditionalSearchFields() {
        return additionalSearchFields;
    }

    /**
     * Determines if the collection contains the given additional search field.
     *
     * @param additional_search_field
     *            The name of the additional search field to check for existence.
     * @return True if the additional search field exists.
     */
    public boolean hasAdditionalSearchField(String additional_search_field) {
        return additionalSearchFields.containsKey(additional_search_field);
    }

    /**
     * Get a map of all search fields for this collection.
     *
     * @return A map of all search fields and additional search fields in this
     *         collection.
     */
    protected Map<String, SearchFieldType> getAllSearchFields() {
        Map<String, SearchFieldType> allSearchFields = new HashMap<String, SearchFieldType>();

        // Add search fields to the schema.
        for (Entry<String, SearchFieldType> entry : searchFields.entrySet()) {
            String name = entry.getKey();
            SearchFieldType value = entry.getValue();

            allSearchFields.put(name, value);
        }

        // Add additional fields to the schema.
        if (additionalSearchFields != null) {
            for (Entry<String, SearchFieldType> entry : additionalSearchFields.entrySet()) {
                String name = entry.getKey();
                SearchFieldType value = entry.getValue();
                allSearchFields.put(name, value);
            }
        }

        return allSearchFields;
    }

    /**
     * Permanently deletes all the documents that are stored in a collection and destroys
     * the collection.
     *
     * @throws JSONStoreRemoveCollectionException
     *             An error occurred when finding the collection to remove (may have
     *             already been removed).
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to remove
     *             the collection.
     * @throws JSONStoreTransactionFailureException
     * 	           A transaction is in process and cannot be accessed to remove the
     * 	           collection.
     */
    public void removeCollection() throws JSONStoreRemoveCollectionException, JSONStoreDatabaseClosedException, JSONStoreTransactionFailureException {

       JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_REMOVE_COLLECTION);
        try {
            DatabaseAccessor acc = getAccessor();
            if(initializedJSONStoreInstance.isTransactionInProgress()){
                throw new JSONStoreTransactionFailureException("Cannot remove collection during a transaction.");
            }
            acc.dropTable();
            initializedJSONStoreInstance.removeCollectionReference(this);
        } finally {
            logInst.end();
        }

    }

    /**
     * Removes all documents from a collection, but does not destroy the collection.
     *
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to clear
     *             the collection.
     */
    public void clearCollection() throws JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_CLEAR);
        try {
            DatabaseAccessor acc = getAccessor();
            acc.getRawDatabase().delete(getName(), "1", new String[] {}); //$NON-NLS-1$
        } finally {
            logInst.end();
        }
    }


    /**
     * Change a list of documents in the collection.
     *
     * @param data
     *            An array of data to update documents in the collection.
     * @return The total number of documents that are changed.
     * @throws JSONStoreChangeException
     *            An exception occurred when performing the change operation, and the
     *            operation failed.
     * @throws JSONStoreDatabaseClosedException
     *            The JSONStore database is closed and cannot be accessed to execute
     *            the change operation.
     */

    public int changeData(JSONObject[] data) throws JSONStoreChangeException, JSONStoreDatabaseClosedException, JSONException {
        return changeData(data, null);
    }

    /**
     * Change a list of documents in the collection.
     *
     * @param data
     *            A JSONObject array of data to update documents in the collection or add
     *            (if addNew option is enabled).
     * @param options
     *            Optional change options that are used to manipulate the change operation.
     * @return The total number of documents that are changed or added.
     * @throws JSONStoreChangeException
     *            An exception occurred when performing the change operation, and the
     *            operation failed.
     * @throws JSONStoreDatabaseClosedException
     *            The JSONStore database is closed and cannot be accessed to execute
     *            the change operation.
     */
    public int changeData(JSONObject[] data, JSONStoreChangeOptions options) throws JSONStoreChangeException, JSONStoreDatabaseClosedException, JSONException {
        return changeData(JSONStoreUtil.convertJSONObjectArrayToJSONObjectList(data), options);
    }

    /**
     * Change a list of documents in the collection.
     *
     * @param data
     *            A list of data to update documents in the collection.
     * @return The total number of documents that are changed.
     * @throws JSONStoreChangeException
     *            An exception occurred when performing the change operation, and the
     *            operation failed.
     * @throws JSONStoreDatabaseClosedException
     *            The JSONStore database is closed and cannot be accessed to execute
     *            the change operation.
     */
    public int changeData(List<JSONObject> data) throws JSONStoreChangeException, JSONStoreDatabaseClosedException, JSONException {
        return changeData(data, null);
    }


    /**
     * Change a list of documents in the collection.
     * @param data
     *            A list of data to update documents in the collection or add (if addNew
     *            option is enabled).
     * @param options
     *            Optional change options that are used to manipulate the change operation.
     * @return The total number of documents that are changed or added.
     * @throws JSONStoreChangeException
     *            An exception occurred when performing the change operation, and the
     *            operation failed.
     * @throws JSONStoreDatabaseClosedException
     *            The JSONStore database is closed and cannot be accessed to execute
     *            the change operation.
     */
    public int changeData(List<JSONObject> data, JSONStoreChangeOptions options) throws JSONStoreChangeException, JSONStoreDatabaseClosedException {


        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_CHANGE);
        try {
            getAccessor(); //This does some closed checks
            if (options == null) options = new JSONStoreChangeOptions();
            if (data == null) {
                return 0;
            }

            try{
                int changeCount = 0;

                if(!initializedJSONStoreInstance.isTransactionInProgress()){
                    synchronized(getAccessor()){
                        getAccessor().getRawDatabase().beginTransaction();
                    }
                }


                List<String> replaceCriteria = options.getSearchFieldCriteria();
                if (replaceCriteria == null || replaceCriteria.size() == 0) {
                    replaceCriteria = new LinkedList<String>();
                }

                //Check to make sure the replaceCriteria are search keys/additional search keys
                for (String replaceKey : replaceCriteria) {
                    if (!getSearchFields().containsKey(replaceKey.toLowerCase(Locale.ENGLISH)) && !getAdditionalSearchFields().containsKey(replaceKey.toLowerCase(Locale.ENGLISH)) &&
                            !getSearchFields().containsKey(replaceKey) && !getAdditionalSearchFields().containsKey(replaceKey)) {
                        String message = "Replace criteria '" + replaceKey + "' must be a search field or additional search field.";
                        JSONStoreChangeException jsException = new JSONStoreChangeException(message);
                        logger.logError(message, jsException);
                        throw jsException;
                    }
                }

                List<JSONObject> allReplaceObjects = new LinkedList<JSONObject>();
                List<JSONObject> allAddObjects = new LinkedList<JSONObject>();
                for (JSONObject currentData : data) {
                    // Build a select against this document.

                    JSONStoreQueryParts queryContent = new JSONStoreQueryParts();
                    JSONStoreQueryPart queryContentPart = new JSONStoreQueryPart();

                    for (String criteria : replaceCriteria) {
                        if (currentData.has(criteria)) {
                            Object val = currentData.get(criteria);
                            if(val instanceof Boolean) val = (Boolean)val ? 1 : 0;
                            queryContentPart.addEqual(criteria, val.toString());

                        }
                    }

                    queryContent.addQueryPart(queryContentPart);
                    QueryBuilderSelect selectQuery = new QueryBuilderSelect(this, queryContent);
                    selectQuery.addSelectStatement(DatabaseConstants.FIELD_ID, false);

                    Cursor allDocIds = runQuery(selectQuery);
                    if (allDocIds != null) {
                        if (allDocIds.getCount() <= 0 || replaceCriteria.size() == 0) {
                            allAddObjects.add(currentData);
                        } else {
                            allDocIds.moveToFirst();
                            for (int i = 0; i < allDocIds.getCount() && !allDocIds.isAfterLast(); ++i) {
                                int idIndex = allDocIds.getColumnIndex(DatabaseConstants.FIELD_ID);
                                int replaceId = allDocIds.getInt(idIndex);
                                JSONObject fullUpdateDoc = new JSONObject();
                                fullUpdateDoc.put(DatabaseConstants.FIELD_JSON, currentData);
                                fullUpdateDoc.put(DatabaseConstants.FIELD_ID, replaceId);
                                allReplaceObjects.add(fullUpdateDoc);
                                allDocIds.moveToNext();
                            }
                        }

                        allDocIds.close();
                    }

                }

                JSONStoreReplaceOptions replaceOptions = new JSONStoreReplaceOptions();
                replaceOptions.setMarkDirty(options.isMarkDirty());
                for (JSONObject doc : allReplaceObjects) {
                    try {
                        if(replaceCriteria.size() != 0) {
                            this.replaceDocument(doc, replaceOptions);
                            changeCount++;
                        }
                    } catch (JSONStoreReplaceException e) {
                        String message = "Failed to replace an existing document.";
                        JSONStoreChangeException jsException = new JSONStoreChangeException(message, e);
                        logger.logError(message, jsException);
                        throw jsException;
                    }
                }

                JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
                addOptions.setMarkDirty(options.isMarkDirty());
                if (options.isAddNew()) {
                    for (JSONObject doc : allAddObjects) {
                        try {
                            this.addData(doc, addOptions);
                            changeCount++;
                        } catch (JSONStoreAddException e) {
                            String message = "Failed to add a new document.";
                            JSONStoreChangeException jsException = new JSONStoreChangeException(message, e);
                            logger.logError(message, jsException);
                            throw jsException;
                        }
                    }
                }

                if(!initializedJSONStoreInstance.isTransactionInProgress()){
                    synchronized(getAccessor()){
                        getAccessor().getRawDatabase().setTransactionSuccessful();
                        getAccessor().getRawDatabase().endTransaction();
                    }
                }

                return changeCount;
            }
            catch(Throwable e){
                if(!initializedJSONStoreInstance.isTransactionInProgress()){
                    synchronized(getAccessor()){
                        getAccessor().getRawDatabase().endTransaction();
                    }
                }

                throw new JSONStoreChangeException(e);
            }
        } finally {
           logInst.end();
        }
    }

    /**
     * Find a document in the collection based on the given ID.
     *
     * @param id
     *             The unique id that is associated with a document in the database (the _id
     *             search field).
     * @return The JSONObject that is associated with the document. Returns null if it is not found.
     * @throws JSONStoreFindException
     *             There was an error executing the find operation.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public JSONObject findDocumentById(int id) throws JSONStoreDatabaseClosedException, JSONStoreFindException {
        List<Integer> idList = new ArrayList<Integer>(1);
        idList.add(id);

        List<JSONObject> results = findDocumentsById(idList);
        if (results.size() > 0) {
            return results.get(0);
        }

        return null;
    }

    /**
     * Find documents in the collection that have the given ID.
     *
     * @param ids
     *             An array of unique ids that are associated with a document in the database (the _id
     *             search field).
     * @return All documents with the given ids that are present in the collection.
     * @throws JSONStoreFindException
     *             There was an error executing the find operation.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findDocumentsById(int[] ids) throws JSONStoreDatabaseClosedException, JSONStoreFindException {
        if(ids == null) {
            ids = new int[0];
        }

        List<Integer> docIdList = new ArrayList<Integer>(ids.length);
        return findDocumentsById(docIdList);
    }

    /**
     * Find all documents in the collection that have the given ids.
     *
     * @param ids
     *             A list of unique ids that are associated with a document in the database (the _id
     *             search field).
     * @return All documents with the given ids that are present in the collection.
     * @throws JSONStoreFindException
     *             There was an error executing the find operation.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findDocumentsById(List<Integer> ids) throws JSONStoreDatabaseClosedException, JSONStoreFindException {
        getAccessor(); // This does some closed checks.

        List<JSONObject> resultList = new LinkedList<JSONObject>();


        List<List<Integer>> idChunks = JSONStoreUtil.splitListIntoChunks(ids, FIND_BY_ID_CHUNK_SIZE);
        for (List<Integer> idChunk : idChunks) {
            try {

                JSONStoreQueryParts content = new JSONStoreQueryParts();
                // Add the ids to the query:
                for (Integer id : idChunk) {

                    JSONStoreQueryPart part = new JSONStoreQueryPart();
                    part.addEqual(DatabaseConstants.FIELD_ID, id);
                    content.addQueryPart(part);
                }

                List<JSONObject> resultsRaw = this.findDocuments(content);

                //Preserving order is important here to maintain backwards compatibility
                HashMap<Integer, JSONObject> resultsMap = new HashMap<Integer, JSONObject>();
                for(JSONObject result : resultsRaw) {
                    resultsMap.put(result.getInt(DatabaseConstants.FIELD_ID), result);
                }

                for(Integer id : ids) {
                    if(resultsMap.containsKey(id)) {
                        resultList.add(resultsMap.get(id));
                    }
                }

            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

    /**
     * Find all the dirty documents in the collection.
     *
     * @return A list of documents that are marked dirty in the collection.
     * @throws JSONStoreFindException
     *             An error occurred when trying to execute the find.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findAllDirtyDocuments() throws JSONStoreFindException, JSONStoreDatabaseClosedException {

        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_FIND_ALL_DIRTY);
        try {
            getAccessor(); // This does some closed checks.
            JSONStoreQueryParts content = new JSONStoreQueryParts();
            JSONStoreQueryPart part = new JSONStoreQueryPart();
            part.addGreaterThan(DatabaseConstants.FIELD_DIRTY, 0);
            content.addQueryPart(part);

            JSONStoreFindOptions options = new JSONStoreFindOptions();
            options.addSearchFilter(DatabaseConstants.FIELD_ID);
            options.addSearchFilter(DatabaseConstants.FIELD_JSON);
            options.addSearchFilter(DatabaseConstants.FIELD_OPERATION);
            options.addSearchFilter(DatabaseConstants.FIELD_DIRTY);
            options.includeDeletedDocuments(true);


            try {
                return findDocuments(content, options);
            } catch (JSONStoreFilterException e) {
                throw new JSONStoreFindException("Error occured filtering results", e);
            }
        } finally {
            logInst.end();
        }
    }

    /**
     * Find all documents in the collection.
     *
     * @return A list of all the documents in the collection.
     * @throws JSONStoreFindException
     * 				  An error occurred when trying to execute the find.
     * @throws JSONStoreDatabaseClosedException
     *                The JSONStore database is closed, and cannot be accessed to
     *                execute the find.
     */
    public List<JSONObject> findAllDocuments() throws JSONStoreFindException, JSONStoreDatabaseClosedException {
        return findAllDocuments(null);
    }

    /**
     * Find all documents in the collection.
     * @param options
     *             Additional options to modify the count operation.
     * @return The list of documents in the collection.
     * @throws JSONStoreFindException
     *             An error occurred when trying to execute the find.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findAllDocuments(JSONStoreFindOptions options) throws JSONStoreFindException, JSONStoreDatabaseClosedException {
        getAccessor(); // This does some closed checks.

        try {
            return findDocuments(null, options);
        } catch (JSONStoreFilterException e) {
            throw new JSONStoreFindException("Error occured filtering results", e);
        }
    }

    /**
     * Find documents in the collection that are based on the given query.
     *
     * @param query
     *             The find query that restricts the search.
     * @return A list of documents.
     * @throws JSONStoreFindException
     *             An error occurred when trying to execute the find.
     * @throws JSONStoreFilterException
     *             An error occurred when trying to apply a filter to the query.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findDocuments(JSONStoreQueryParts query) throws JSONStoreFindException, JSONStoreFilterException, JSONStoreDatabaseClosedException {
        return findDocuments(query, null);
    }

    /**
     * Find documents in the collection that are based on the given query.
     *
     * @param query
     *             The find query that restricts the search.
     * @param options
     *             Additional options to modify the count operation.
     * @return	A list of documents.
     * @throws JSONStoreFindException
     *             An error occurred when trying to execute the find.
     * @throws JSONStoreFilterException
     *             An error occurred when trying to apply a filter to the query.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             execute the find.
     */
    public List<JSONObject> findDocuments(JSONStoreQueryParts query, JSONStoreFindOptions options) throws JSONStoreFindException, JSONStoreFilterException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_FIND);
        try {
            getAccessor(); // This does some closed checks.
            if(options == null) {
                options = new JSONStoreFindOptions();
            }

            if(query == null) {
                query = new JSONStoreQueryParts();
            }

            QueryBuilderSelect selectQuery = new QueryBuilderSelect(this, query);
            selectQuery.setLimit(options.getLimit());
            selectQuery.setOffset(options.getOffset());
            selectQuery.setSort(options.getSort());

            if(options.shouldIncludeDeletedDocuments()) {
                selectQuery.setSearchIncludeDeleted();
            }


            LinkedHashMap<Integer, JSONObject> resultHash = new LinkedHashMap<Integer, JSONObject> ();
            List<JSONObject> filterResults = new ArrayList<JSONObject>();

            // Set the fields to select in the query, if specified, otherwise default to _id and _json:
            Map<String, Boolean> filters = options.getSearchFilters();
            if (filters != null && filters.size() > 0) {
                for (String filter : filters.keySet()) {
                    boolean isSpecial = filters.get(filter);
                    selectQuery.addSelectStatement(filter, isSpecial);
                }
            }else {
                selectQuery.addSelectStatement(DatabaseConstants.FIELD_ID, false);
                selectQuery.addSelectStatement(DatabaseConstants.FIELD_JSON, false);
            }

            Cursor cursor = null;
            List<JSONObject> result = null;
            try {
                cursor = runQuery(selectQuery);
                if (cursor != null) {
                    result = new LinkedList<JSONObject>();

                    for (int j = 0; j < cursor.getCount(); ++j) {
                        JSONObject item = new JacksonSerializedJSONObject();

                        cursor.moveToNext();

                        for(int k = 0; k < cursor.getColumnNames().length; ++k) {
                            if(cursor.getColumnName(k).equals(DatabaseConstants.FIELD_ID)) {
                                item.put(cursor.getColumnName(k), cursor.getInt(k));
                            }else if(cursor.getColumnName(k).equals(DatabaseConstants.FIELD_JSON)){
                                item.put(DatabaseConstants.FIELD_JSON, JsonOrgModule.deserializeJSONObject(cursor.getString(k)));
                            }else if(isJSONCreatedColumn(cursor.getColumnName(k))){
                                item.put(cursor.getColumnName(k), cursor.getString(k));
                            } else {
                                item.put(cursor.getColumnName(k).replace("_", "."), cursor.getString(k));  //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }

                        result.add(item);
                    }
                }
            } catch (Throwable e) {
                String message = "Error when attempting to find a document. An error occurred when reading from the database.";
                JSONStoreFindException jsException = new JSONStoreFindException(message, e);
                logger.logError(message, jsException);
                throw jsException;

            } finally {
                if (cursor != null) cursor.close();
            }


            if(options.getSearchFilters() != null) {
                for(JSONObject obj : result) {
                    filterResults.add(obj);
                }
            }else {
                addNonDuplicates(resultHash, result);
            }

            List<JSONObject> results = null;

            if(options.getSearchFilters() != null)
            {
                results = removeFilterDuplicates(filterResults);
            }else {
                results = new ArrayList<JSONObject>(resultHash.values());
            }

            return results;
        } finally {
            logInst.end();
        }
    }


    /**
     * Determine if a document is dirty or not.
     *
     * @param id
     *             The unique identifier of the document to check.
     * @return
     * 				Returns true if the given document is dirty.
     * @throws JSONStoreDirtyCheckException
     *             An error occurred when checking to see if the document is dirty.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to check
     *             if the document is dirty.
     */
    public boolean isDocumentDirty(int id) throws JSONStoreDirtyCheckException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_IS_DOCUMENT_DIRTY);
        try {
            getAccessor(); // This does some closed checks.

            JSONStoreQueryParts content = new JSONStoreQueryParts();
            JSONStoreQueryPart part = new JSONStoreQueryPart();

            part.addEqual(DatabaseConstants.FIELD_ID, id);
            part.addGreaterThan(DatabaseConstants.FIELD_DIRTY, 0);

            content.addQueryPart(part);
            List<JSONObject> result;
            try {
                result = findDocuments(content);
            } catch (JSONStoreException e) {
                throw new JSONStoreDirtyCheckException("An error occured finding the document", e);
            }

            if(result.size() > 0){
                return true;
            }

            return false;
        } finally {
            logInst.end();
        }
    }

    /**
     * Mark an array of documents in the collection clean.
     *
     * @param documents
     *             An array of documents to clean in the collection.
     * @return
     *             Returns the number of clean documents.
     * @throws JSONStoreMarkCleanException
     *             An error occurred when trying to clean the documents that were passed.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to mark
     *             the documents clean.
     */

    public int markDocumentsClean(JSONObject[] documents) throws JSONStoreMarkCleanException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_MARK_CLEAN);
        try {
            List<JSONObject> docs = JSONStoreUtil.convertJSONObjectArrayToJSONObjectList(documents);
            return markDocumentsClean(docs);
        } finally {
            logInst.end();
        }

    }

    /**
     * Mark a list of documents in the collection clean.
     *
     * @param documents
     *             A list of documents to clean in the collection.
     * @return
     * 			  Returns the number of clean documents.
     * @throws JSONStoreMarkCleanException
     *             An error occurred when trying to clean the documents that were passed.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to mark
     *             the documents clean.
     */
    public int markDocumentsClean(List<JSONObject> documents) throws JSONStoreMarkCleanException, JSONStoreDatabaseClosedException {
        int numOfCleanDocs = 0;
        for (JSONObject doc : documents) {
            numOfCleanDocs+=markDocumentClean(doc);

        }

        return numOfCleanDocs;
    }

    /**
     * Mark a document in the collection clean.
     *
     * @param document
     *             A document to clean in the collection.
     * @return
     *             Returns 1 if the document has been cleaned.
     * @throws JSONStoreMarkCleanException
     *             An error occurred when trying to clean the documents that were passed.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to mark
     *             the documents clean.
     */
    public int markDocumentClean(JSONObject document) throws JSONStoreMarkCleanException, JSONStoreDatabaseClosedException {
        if (document == null) return 0;

        int id;
        String operation = null;
        try {
            id = document.getInt(DatabaseConstants.FIELD_ID);
            operation = document.getString(DatabaseConstants.FIELD_OPERATION);
            if (operation == null) {
                String message = "Document does not contain the operation to execute.";
                JSONStoreMarkCleanException jsException = new JSONStoreMarkCleanException(message);
                logger.logError(message, jsException);
                throw jsException;
            }

        } catch (JSONException e) {
            String message = "Could not parse the document.";
            JSONStoreMarkCleanException jsException = new JSONStoreMarkCleanException(message, e);
            logger.logError(message, jsException);
            throw jsException;
        }

        DatabaseAccessor acc = getAccessor();
        WritableDatabase db = acc.getWritableDatabase();
        if (operation.equals(DatabaseConstants.OPERATION_REMOVE)) {
            // The record will be completely removed from the database.
            db.delete(new String[] { DatabaseConstants.FIELD_ID }, new Object[] { id });
        }

        // Otherwise, we just need to update the fields to "clean".

        HashMap<String, Object> whereClauses = new HashMap<String, Object>();
        whereClauses.put(DatabaseConstants.FIELD_ID, id);
        db.update(new String[] { DatabaseConstants.FIELD_DIRTY, DatabaseConstants.FIELD_DELETED, DatabaseConstants.FIELD_OPERATION }, new Object[] { 0, 0, "" }, whereClauses); //$NON-NLS-1$

        return 1;
    }

    /**
     * Count the number of documents in the collection.
     *
     * @return The total number of documents that are currently in the database that are not
     *         marked dirty.
     * @throws JSONStoreCountException
     *             A failure occurred when trying to determine the count.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the count.
     */
    public int countAllDocuments() throws JSONStoreCountException, JSONStoreDatabaseClosedException {
        return countDocuments(null, null);
    }

    /**
     * Count the number of documents in the collection that are based on the given query.
     *
     * @param query
     *             The count query that limits the count scope.
     * @return The total number of documents that are currently in the database that are not
     *         marked dirty.
     * @throws JSONStoreCountException
     *             A failure occurred when trying to determine the count.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the count.
     */
    public int countDocuments(JSONStoreQueryParts query) throws JSONStoreCountException, JSONStoreDatabaseClosedException {
        return countDocuments(query, null);
    }

    /**
     * Count the number of documents in the collection that are based on the given query.
     * @param query
     *             The find query that limits the count scope.
     * @param options
     *             Additional options to modify the count operation.
     * @return The total number of documents that are currently in the database that are not
     *         marked dirty and fit the find query.
     * @throws JSONStoreCountException
     *             A failure occurred when trying to determine the count.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the count.
     */
    public int countDocuments(JSONStoreQueryParts query, JSONStoreCountOptions options) throws JSONStoreCountException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_COUNT);

        try {
            getAccessor(); // This does some closed checks.
            if (query == null) {
                query = new JSONStoreQueryParts();
            }
            if (options == null) options = new JSONStoreCountOptions();

            JSONStoreFindOptions findOptions = new JSONStoreFindOptions();
            if(options.shouldIncludeDeletedDocuments()) {
                findOptions.includeDeletedDocuments(true);
            }

            findOptions.addSearchFilterSpecial(DatabaseConstants.SQL_COUNT);

            try {
                List<JSONObject> results = this.findDocuments(query, findOptions);

                if(results != null && results.size() == 1) {
                    JSONObject countResults = results.get(0);
                    if(countResults == null || countResults.opt(DatabaseConstants.SQL_COUNT) == null) {
                        throw new JSONStoreCountException("Could not count the results. Missing count return value internally");
                    }

                    return countResults.getInt(DatabaseConstants.SQL_COUNT);
                } else {
                    throw new JSONStoreCountException("Could not count the results. Missing results from find internally");
                }
            } catch(JSONStoreFilterException e) {
                throw new JSONStoreCountException("Could not count the results. Filter exception occured internally", e);
            } catch(JSONStoreFindException e) {
                throw new JSONStoreCountException("Could not count the results. Find exception occured internally",e);
            } catch(JSONException e) {
                throw new JSONStoreCountException("Could not count the results. JSONException occured internally", e);

            }
        } finally {
            logInst.end();
        }
    }

    /**
     * Count the number of dirty documents in the collection.
     *
     * @return
     *             The number of dirty documents.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the count.
     * @throws JSONStoreCountException
     *             A failure occurred when trying to determine the count.
     */
    public int countAllDirtyDocuments() throws JSONStoreDatabaseClosedException, JSONStoreCountException {

        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_COUNT_ALL_DIRTY);
        try {
            getAccessor(); // This does some closed checks.
            JSONStoreQueryParts queryContent = new JSONStoreQueryParts();
            JSONStoreQueryPart queryContentPart = new JSONStoreQueryPart();
            queryContentPart.addGreaterThan(DatabaseConstants.FIELD_DIRTY, 0);
            queryContent.addQueryPart(queryContentPart);

            JSONStoreCountOptions options = new JSONStoreCountOptions();
            options.includeDeletedDocuments(true);

            return countDocuments(queryContent, options);
        } finally {
            logInst.end();
        }
    }

    /**
     * Add data and create a new document in the collection.
     *
     * @param object_to_add
     *             The data for a new document to be added to the collection. The document must contain search
     *             field keys.
     * @throws JSONStoreAddException
     *             Thrown if the document could not be added to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the add.
     */
    public void addData(JSONObject object_to_add) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        List<JSONObject> data = new ArrayList<JSONObject>();
        data.add(object_to_add);

        addData(data, null);
    }

    /**
     * Add data and create a new document in the collection.
     *
     * @param data
     *             The data for a new document to be added to the collection. The document must contain search
     *             field keys.
     * @param options
     *             Additional options to modify the add operation.
     * @throws JSONStoreAddException
     *             Thrown if the document could not be added to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the add.
     */
    public void addData(List<JSONObject> data, JSONStoreAddOptions options) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_ADD);
        try {
            if (options == null) options = new JSONStoreAddOptions();
            if(data == null || data.size() <= 0) return;
            DatabaseAccessor acc = getAccessor();

            if(!initializedJSONStoreInstance.isTransactionInProgress()){
                acc.getRawDatabase().beginTransaction();
            }

            int numberOfDocumentsStored = 0;

            try {
                for(JSONObject data_to_add : data){
                    if(data_to_add == null){
                        continue;
                    }

                    Map<String, Object> mappedObj = null;
                    try {
                        mappedObj = schema.mapObject(data_to_add, options.getAdditionalSearchFieldsAsJSON());
                        mappedObj.put(DatabaseConstants.FIELD_JSON, data_to_add.toString());
                    } catch (Throwable t) {
                        String message = "An internal error occurred when trying to store the JSONObject. Error mapping the search fields.";
                        JSONStoreAddException jsException = new JSONStoreAddException(message, t);
                        logger.logError(message, jsException);
                        throw jsException;
                    }

                    if (options.isMarkDirty()) {
                        mappedObj.put(DatabaseConstants.FIELD_DIRTY, new Date().getTime());
                        mappedObj.put(DatabaseConstants.FIELD_OPERATION, DatabaseConstants.OPERATION_ADD);
                    }

                    else {
                        mappedObj.put(DatabaseConstants.FIELD_DIRTY, 0);
                        mappedObj.put(DatabaseConstants.FIELD_OPERATION, DatabaseConstants.OPERATION_STORE);
                    }

                    ContentValues contentValues = new ContentValues();
                    Iterator<String> keys = mappedObj.keySet().iterator();

                    while (keys.hasNext()) {
                        String key = keys.next();

                        // WL 6.0 change: Handle booleans like iOS, turn true into 1 and
                        // false into 0.
                        Object val = mappedObj.get(key);
                        if (val instanceof Boolean) {
                            val = (Boolean) val ? 1 : 0;
                        }

                        contentValues.put("'" + JSONStoreUtil.getDatabaseSafeSearchFieldName(key) + "'", val.toString()); //$NON-NLS-1$ //$NON-NLS-2$

                    }

                    long rc = acc.getRawDatabase().insert(getName(), null, contentValues);

                    if (rc == -1) { // no error
                        String message = "An internal error occurred when trying to insert a document.";
                        JSONStoreAddException jsException = new JSONStoreAddException(message, numberOfDocumentsStored);
                        logger.logError(message, jsException);
                        throw jsException;
                    }

                    numberOfDocumentsStored++;
                }

                if(!initializedJSONStoreInstance.isTransactionInProgress()){
                    acc.getRawDatabase().setTransactionSuccessful();
                }
            }
            finally{
                // Commit or roll back transaction, depending on whether it was set successful or not:
                if(!initializedJSONStoreInstance.isTransactionInProgress()){
                    acc.getRawDatabase().endTransaction();
                }
            }
        } finally {
            logInst.end();
        }
    }

    public void addData(JSONObject object_to_add, JSONStoreAddOptions opts) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        List<JSONObject> data = new ArrayList<JSONObject>();
        data.add(object_to_add);

        addData(data, opts);
    }

    public void addData(List<JSONObject> object_to_add) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        addData(object_to_add, null);
    }

    public void addData(JSONArray dataArray) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        List<JSONObject> data = JSONStoreUtil.convertJSONArrayToJSONObjectList((JSONArray) dataArray);

        addData(data, null);
    }

    public void addData(JSONArray dataArray, JSONStoreAddOptions opts) throws JSONStoreAddException, JSONStoreDatabaseClosedException {
        List<JSONObject> data = JSONStoreUtil.convertJSONArrayToJSONObjectList((JSONArray) dataArray);

        addData(data, opts);
    }

    /**
     * Remove a document from a collection that is based on the given id.
     *
     * @param id the integer id
     * @return the number of documents removed from the collection.
     * @throws JSONStoreRemoveException
     *             Thrown if the document could not be removed to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the remove.
     */
    public int removeDocumentById(Integer id) throws JSONStoreRemoveException, JSONStoreDatabaseClosedException {
        return removeDocumentById(id, null);
    }

    /**
     * Remove a document from the collection that is based on the given id.
     *
     * @param id the integer id
     * @param options Additional options to modify the remove operation.
     * @return the number of documents removed from the collection.
     * @throws JSONStoreRemoveException
     *             Thrown if the document could not be removed to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the remove.
     */
    public int removeDocumentById(Integer id, JSONStoreRemoveOptions options) throws JSONStoreRemoveException, JSONStoreDatabaseClosedException {
        List<Integer> list = new ArrayList<Integer>(1);
        list.add(id);
        return removeDocumentsById(list, options);
    }

    /**
     * Remove list of documents from the collection that are based on the given ids.
     *
     * @param document_ids
     * 				A list of document ids.
     * @return	The number of documents that have been removed from the collection.
     * @throws JSONStoreRemoveException
     *             Thrown if the document could not be removed to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the remove.
     */
    public int removeDocumentsById(List<Integer> document_ids) throws JSONStoreRemoveException, JSONStoreDatabaseClosedException {
        return removeDocumentsById(document_ids, null);
    }

    /**
     * Remove list of documents from the collection that are based on the given ids.
     *
     * @param document_ids
     * 			A list of document ids.
     * @param options
     * 			Additional options to modify the remove operation.
     * @return	The number of documents that have been removed from the collection.
     * @throws JSONStoreRemoveException
     *             Thrown if the document could not be removed to the collection.
     *             Commonly thrown if search fields are missing in the document.
     *             The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The JSONStore database is closed, and cannot be accessed to
     *             perform the remove.
     */
    public int removeDocumentsById(List<Integer> document_ids, JSONStoreRemoveOptions options) throws JSONStoreRemoveException, JSONStoreDatabaseClosedException {
        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_REMOVE);
        try {
            DatabaseAccessor accessor = getAccessor();
            if(options == null) {
                options = new JSONStoreRemoveOptions();
            }

            List<JSONObject> documents = new LinkedList<JSONObject>();
            try {
                documents = findDocumentsById(document_ids);
            } catch (JSONStoreFindException e) {
                throw new JSONStoreRemoveException("Could not execute find on document ids", e);
            }

            List<JSONObject> failures = new LinkedList<JSONObject>();
            int removedCount = 0;

            if(!initializedJSONStoreInstance.isTransactionInProgress()){
                try{
                    accessor.getRawDatabase().beginTransaction();
                }
                catch(Throwable e){
                    throw new JSONStoreRemoveException(e);
                }
            }

            // Loop through the list of objects. Each object could reference a
            // single object (with or without an _id) or could
            // contain query parms (without an _id) that map to a set of multiple
            // actual DB objects.
            for (JSONObject documentToRemove : documents) {
                if(documentToRemove == null) continue;


                try {
                    removedCount += accessor.getWritableDatabase().deleteIfRequired(documentToRemove, !options.isMarkDirty(), true);
                } catch (Throwable e) {
                    // The update failed, so add the document to the list of failures.
                    String message = "Error while removing/deleting document in collection \"" + getName() + "\".";
                    logger.logTrace(message);
                    failures.add(documentToRemove);

                    // Roll back the transaction.
                    if(initializedJSONStoreInstance.isTransactionInProgress()){
                        try{
                            accessor.getRawDatabase().endTransaction();
                        }
                        catch(Throwable e1){
                            throw new JSONStoreRemoveException(e1);
                        }
                    }
                }
            }

            if (failures.size() > 0) {
                // Roll back the transaction.
                if(initializedJSONStoreInstance.isTransactionInProgress()){
                    try{
                        accessor.getRawDatabase().endTransaction();
                    }
                    catch(Throwable e1){
                        throw new JSONStoreRemoveException(e1);
                    }
                }

                String message = "At least one document could not be removed.";
                JSONStoreRemoveException jsException = new JSONStoreRemoveException(message, failures);
                logger.logError(message, jsException);
                throw jsException;
            }

            // Commit the transaction.
            if(!initializedJSONStoreInstance.isTransactionInProgress()){
                try{
                    accessor.getRawDatabase().setTransactionSuccessful();
                    accessor.getRawDatabase().endTransaction();
                }
                catch(Throwable e1){
                    throw new JSONStoreRemoveException(e1);
                }
            }

            return removedCount;
        } finally {
            logInst.end();
        }
    }


    /**
     * Replace a document in the collection.
     *
     * @param document
     *             A JSONObject that represents the document to update in the collection.
     *             The document's '_id' is used to determine which document to
     *             replace.
     * @return The number of replaced documents.
     * @throws JSONStoreReplaceException
     *             The document could not be replaced. The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The collection is currently closed.
     */
    public int replaceDocument(JSONObject document) throws JSONStoreReplaceException, JSONStoreDatabaseClosedException {
        return replaceDocument(document, null);
    }

    /**
     * Replace a document in the collection.
     *
     * @param document
     *             A JSONObject that represents the document to update in the collection.
     *             The document's '_id' is used to determine which document to
     *             replace.
     * @param options
     *             Additional options to modify the replace operation.
     * @return The number of replaced documents.
     * @throws JSONStoreReplaceException
     *             The document could not be replaced. The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The collection is currently closed.
     */
    public int replaceDocument(JSONObject document, JSONStoreReplaceOptions options) throws JSONStoreReplaceException, JSONStoreDatabaseClosedException {
        if (document == null) return 0;

        List<JSONObject> docList = new ArrayList<JSONObject>(1);
        docList.add(document);
        return replaceDocuments(docList, options);
    }

    /**
     * Replace a list of documents in the collection.
     *
     * @param documents
     *             A List that contains JSONObjects that represent the documents to update
     *             in the collection. The document's '_id' is used to determine which
     *             document to replace. If all documents could not be updated, a
     *             rollback is performed to a state where no documents were updated.
     * @return The number of replaced documents.
     * @throws JSONStoreReplaceException
     *             The document could not be replaced. The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The collection is currently closed.
     */
    public int replaceDocuments(List<JSONObject> documents) throws JSONStoreDatabaseClosedException, JSONStoreReplaceException {
        return replaceDocuments(documents, null);
    }

    /**
     * Replace a list of documents in the collection.
     *
     * @param documents
     *             A List that contains JSONObjects that represent the documents to update
     *             in the collection. The document's '_id' is used to determine which
     *             document to replace. If all documents could not be updated, a
     *             rollback is performed to a state where no documents were updated.
     * @param options
     *             Additional options to modify the replace operation.
     * @return The number of replaced documents.
     * @throws JSONStoreReplaceException
     *             The document could not be replaced. The message contains the reason.
     * @throws JSONStoreDatabaseClosedException
     *             The collection is currently closed.
     */
    public int replaceDocuments(List<JSONObject> documents, JSONStoreReplaceOptions options) throws JSONStoreDatabaseClosedException, JSONStoreReplaceException {
        int updatedDocs = 0;


        JSONStoreAnalyticsLogInstance logInst = JSONStoreLogger.startAnalyticsInstance(getUsername(), getName(), JSONStoreLogger.OPERATION_REPLACE);
        try {

            if(documents == null) {
                return updatedDocs;
            }

            if (options == null) {
                options = new JSONStoreReplaceOptions();
            }

            DatabaseAccessor acc = getAccessor();
            List<JSONObject> failures = new LinkedList<JSONObject>();

            // Iterate over all the documents and replace them.
            acc.getRawDatabase().beginTransaction();
            try {
                for (JSONObject document: documents) {
                    if(document == null) continue;
                    try {
                        updatedDocs++;
                        acc.getWritableDatabase().update(document, options.isMarkDirty());
                    }

                    catch (Throwable e) {
                        String message = "Error while updating document on collection \"" + schema.getName() + "\".";
                        logger.logTrace(message);

                        if (document != null) {
                            failures.add(document);
                        }
                    }
                }

                if (failures.size() != 0) {
                    String message = "At least one document failed to be replaced.";
                    JSONStoreReplaceException jsException = new JSONStoreReplaceException(message, failures);
                    logger.logError(message, jsException);
                    throw jsException;
                }

                acc.getRawDatabase().setTransactionSuccessful();
                return updatedDocs;
            } finally {
                acc.getRawDatabase().endTransaction();
            }
        } finally {
            logInst.end();
        }
    }

}
