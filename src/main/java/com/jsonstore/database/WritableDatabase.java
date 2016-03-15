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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jsonstore.jackson.JacksonSerializedJSONObject;
import com.jsonstore.util.JSONStoreUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WritableDatabase extends ReadableDatabase {
     private static final String SQL_AND = " AND"; //$NON-NLS-1$
     private static final String SQL_DELETE = "DELETE FROM {0} WHERE {1};"; //$NON-NLS-1$
     private static final String SQL_INSERT = "INSERT INTO {0} ({1}) VALUES ({2});"; //$NON-NLS-1$
     private static final String SQL_UPDATE = "UPDATE {0} SET {1}{2};"; //$NON-NLS-1$
     private static final String SQL_WHERE = " WHERE"; //$NON-NLS-1$
     
     protected WritableDatabase (SQLiteDatabase database,
          DatabaseSchema schema) {
          super (database, schema);
          
     }
     
     /*
      * Actually delete the specified objects (not just mark them to be deleted later)
      * 
      * @param whereClause String[] List of query WHERE clauses used to obtain set of objects to delete.
      * @param whereValues String[] List of values to pair with the specified clauses.
      * @returns int Number of objects actually deleted.
      */
     public int delete (String whereClause[], Object whereValues[]) {
          String dbName = getSchema().getName();
          String fixedWhereValues[];
          StringBuilder whereClauseStr = new StringBuilder();
          
          // Build up the where clause.
          
          if ((whereClause == null) || (whereClause.length < 1)) {
               // Take this to mean "delete all".
               
               whereClauseStr.append ("1"); //$NON-NLS-1$
          }
          
          else {
               for (int i = 0; i < whereClause.length; ++i) {
                    whereClauseStr.append (whereClause[i]);
                    whereClauseStr.append (" = ?"); //$NON-NLS-1$
                    
                    if (i < (whereClause.length - 1)) {
                         whereClauseStr.append (WritableDatabase.SQL_AND);
                    }
               }
          }
          
          logDeleteQuery(whereValues, dbName, whereClauseStr);
          
          fixedWhereValues = new String[whereValues.length];
          
          for (int i = 0; i < whereValues.length; ++i) {
               fixedWhereValues[i] = whereValues[i].toString();
          }
          
          return getDatabase().delete (dbName, whereClauseStr.toString(),
               fixedWhereValues);
     }

	private void logDeleteQuery(Object[] whereValues, String dbName,
			StringBuilder whereClauseStr) {
		String sql;
		   
		   // Build up the delete SQL.
		   
		   sql = JSONStoreUtil.formatString (WritableDatabase.SQL_DELETE,
		        dbName, whereClauseStr.toString());
		   
		   logger.logTrace ("executing delete on database \"" + dbName + "\":");
		   logger.logTrace ("   " + sql); //$NON-NLS-1$
		   logger.logTrace ("   args:");
		   
		   for (int i = 0; i < whereValues.length; ++i) {
		        logger.logTrace ("      " + whereValues[i]); //$NON-NLS-1$
		   }
	}
 

     /*
      * Remove the specified single object. If isErase is true or if the object is marked as having been added but not yet
      *   sync'd then the object will actually be deleted. Otherwise it is updated to indicate it should be removed
      *   when this action is sync'd to the server.
      *   
      * @param obj A JSONObject to remove (id + object)
      * @param isErase Boolean to indicate if the object should be deleted or just updated to indicate removal during server sync.
      * @returns Number of objects impacted. On success this will return >= 1. On failure it will return 0 if no objects matched.
      * @throws JSONException If update cannot parse the specified JSONObject  
      */
	public int deleteIfRequired (JSONObject obj, boolean isErase, boolean exact) throws JSONException  {
         boolean doErase = isErase;  // start with the value that was passed in, override if necessary...
         boolean theEasyWay = true;  // if there is an id, we have one object and can optimize the removal.
         int result = 0;
         int id = 0;
         String operation = null;
         
         try {  // try the easy way first, check if there is a single id.
        	 id = obj.getInt (DatabaseConstants.FIELD_ID);
             operation = findOperationForObjectById (id);
         }
         catch (JSONException e) { // No ID was found, it either means no object found or the object didn't contain an ID field.
        	 theEasyWay = false;   // Have to find the object the "hard" way (via query, using the info in obj) 
         }
         
         if (theEasyWay) {
	         if ((operation != null) && (operation.equals(DatabaseConstants.OPERATION_ADD))) {
	             doErase = true;  // ...and force it to "true" if a non-pushed add/store is pending. Don't tag as "pending remove", just delete it.
	         }
	
	         if (doErase) {
	             // We're going to immediately delete the objects.
	             result = delete (new String[] {DatabaseConstants.FIELD_ID },
	                              new Object[] {obj.getInt (DatabaseConstants.FIELD_ID) });
	         } else {
	             // We're not immediately removing the objects, so just mark them as dirty and update the operation to "remove".
	                 result = update (new String[] {
	                                      DatabaseConstants.FIELD_DELETED,
	                                      DatabaseConstants.FIELD_DIRTY,
	                                      DatabaseConstants.FIELD_OPERATION },
	                                  new Object[] { 1, new Date().getTime(), DatabaseConstants.OPERATION_REMOVE },
	                                  id);
	         }
         } else {   // we have to do it the hard way, with a query and possibly multiple matches to loop through, some of which may be marked add, some not.
             String strOp;
             int intId;
             int actionCount = 0;
        	 Cursor removeSet = null;
        	 int setSize;
        	 
        	 // Use the JSONObject info to query a list of matches.
        	 removeSet = findUsingQueryObject(obj,
        			                          new String[] {DatabaseConstants.FIELD_ID,
        			                                        DatabaseConstants.FIELD_OPERATION},
        			                          new String[] {DatabaseConstants.FIELD_DELETED + " = 0" }, null, null, exact); //$NON-NLS-1$
        	 
        	 // Loop through the matches to separate the objects marked "add" from those marked !"add"
             setSize = removeSet.getCount();
             
             for (int i = 0; i < setSize; ++i) {
                  JSONObject item = new JacksonSerializedJSONObject();
                  
                  removeSet.moveToNext();
                  
                  // Get the ID and Operation fields from each object in the set returned by the query.
                  intId = removeSet.getInt(0);
                  strOp = removeSet.getString(1);
                  
                  if ((DatabaseConstants.OPERATION_ADD.compareToIgnoreCase(strOp) == 0) || (isErase)) {
                	  // Actually delete all the matches marked "add" (count them up for return)
     	             result = delete (new String[] {DatabaseConstants.FIELD_ID }, new Object[] {intId });         
                  } else {
                	  // Actually update all the matches marked !"add" (count them up for return)
 	                  result = update (new String[] {DatabaseConstants.FIELD_DELETED,
                                                    DatabaseConstants.FIELD_DIRTY,
                                                    DatabaseConstants.FIELD_OPERATION },
                                      new Object[] { 1, new Date().getTime(), DatabaseConstants.OPERATION_REMOVE },
                                      intId);               	  
                  }
                  actionCount += result; // Keep a running tally of the total number of objects deleted/marked for delete.
             }
             
             result = actionCount;
             removeSet.close();
         }

         return result;        	 
     }
          
     public int update (String columnNames[], Object columnValues[],
          Map<String, Object> whereClauses) {
          String dbName = getSchema().getName();
          ContentValues values = new ContentValues();
          StringBuilder whereClause = null;
          String whereValues[] = null;
          
          // Build up the where clause.
          
          if ((whereClauses != null) && (whereClauses.size() > 0)) {
               int i = 0;
               Iterator<String> keys = whereClauses.keySet().iterator();
               
               // The idea here is that the where clause map is of this format,
               // which is suited to Android's style:
               // "<field> <op> ?" -> value
               
               whereClause = new StringBuilder();
               whereValues = new String[whereClauses.size()];
               
               while (keys.hasNext()) {
                    String key = keys.next();
                    
                    whereClause.append (' ');
                    whereClause.append (key);
                    whereClause.append (" = ?"); //$NON-NLS-1$
                    
                    if (keys.hasNext()) {
                         whereClause.append (WritableDatabase.SQL_AND);
                    }
                    
                    whereValues[i++] = whereClauses.get (key).toString();
               }
          }
          
          logUpdateQuery(columnNames, columnValues, dbName, whereClause, whereValues);
          
          for (int i = 0; i < columnNames.length; ++i) {
               values.put (columnNames[i], columnValues[i].toString());
          }
          
          return getDatabase().update (dbName, values, (whereClause == null) ?
               null : whereClause.toString(), whereValues);
     }

	private void logUpdateQuery(String[] columnNames, Object[] columnValues,
			String dbName, StringBuilder whereClause, String[] whereValues) {
		StringBuilder columnsStr = new StringBuilder();
		   String sql;
		   
		   // Build up the update SQL.
		   
		   for (int i = 0; i < columnNames.length; ++i) {
		        columnsStr.append (columnNames[i]);
		        columnsStr.append (" = "); //$NON-NLS-1$
		        columnsStr.append (columnValues[i]);
		        
		        if (i < (columnNames.length -1)) {
		             columnsStr.append (", "); //$NON-NLS-1$
		        }
		   }
		   
		   sql = JSONStoreUtil.formatString (WritableDatabase.SQL_UPDATE,
		        dbName, columnsStr.toString(), (whereClause == null) ?
		        "" : WritableDatabase.SQL_WHERE + whereClause.toString()); //$NON-NLS-1$
		   
		   logger.logTrace ("executing update on database \"" + dbName + "\":");
		   logger.logTrace ("   sql: " + sql);
		   logger.logTrace ("   arguments:");
		   
		   for (String whereValue : whereValues) {
		        logger.logTrace ("      " + whereValue); //$NON-NLS-1$
		   }
	}
     
     public int update (String columnNames[], Object columnValues[], int id) throws JSONException {
          HashMap<String, Object> newWhereClauses = new HashMap<String, Object>();
          newWhereClauses.put(DatabaseConstants.FIELD_ID,id);
          
          return update (columnNames, columnValues, newWhereClauses);
     }
     
     public int update (Map<String, Object> columns, Map<String, Object>
          whereClauses) {
          String columnNames[] = new String[columns.size()];
          Object columnValues[] = new Object[columns.size()];
          int i = 0;
          Iterator<String> keys = columns.keySet().iterator();
          
          while (keys.hasNext()) {
               String key = keys.next();
               
               columnNames[i] = "["+JSONStoreUtil.getDatabaseSafeSearchFieldName (key)+"]"; //$NON-NLS-1$ //$NON-NLS-2$
               columnValues[i++] = columns.get (key);
          }
          
          return update (columnNames, columnValues, whereClauses);
          
     }
     
     public int update (JSONObject newObj, boolean markDirty) throws Throwable {
          long dirtyTime = (markDirty ? new Date().getTime() : 0);
          int id = newObj.getInt (DatabaseConstants.FIELD_ID);
          Map<String, Object> mappedObj;
          String operation;
          HashMap<String, Object> whereClauses = new HashMap<String, Object>();
          
          // TODO: Verify that this *really* does *always* have an _id present. If not, this will fail ugly.
          
          // The object passed in is actually of the form { _id:, json: }.
          // The object in the json field is the one we really want to update
          // with.
          
          newObj = newObj.getJSONObject (DatabaseConstants.FIELD_JSON);
          mappedObj = getSchema().mapObject (newObj, null);
          
          // Add in all the stuff to indicate that the object has been
          // replaced.
          
          mappedObj.put (DatabaseConstants.FIELD_DIRTY, dirtyTime);
          mappedObj.put (DatabaseConstants.FIELD_ID, id);
          mappedObj.put (DatabaseConstants.FIELD_JSON, newObj.toString());
          
          // See if this object's operation was previously "add".  If so, we
          // don't want to update the operation.
          
          operation = findOperationForObjectById (id);
          
          //TODO: Is operation_remove necessary here?
          if ((operation == null) || operation.equals (DatabaseConstants.OPERATION_REMOVE)) {
        	  // We're either replacing a non-existent record or trying to update something that's
        	  // removed, so abort.
        	  
        	  throw new Throwable();
          }
          
          if (!operation.equals (DatabaseConstants.OPERATION_ADD)) {
               mappedObj.put (DatabaseConstants.FIELD_OPERATION,
                    DatabaseConstants.OPERATION_REPLACE);
          }
          
          whereClauses.put (DatabaseConstants.FIELD_ID, id);
          
          return update (mappedObj, whereClauses);
     }
}
