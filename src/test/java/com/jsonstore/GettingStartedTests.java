/*
 * IBM Confidential OCO Source Materials
 * 
 * 5725-I43 Copyright IBM Corp. 2006, 2013
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 * 
 */

package com.jsonstore;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.jsonstore.api.JSONStore;
import com.jsonstore.api.JSONStoreAddOptions;
import com.jsonstore.api.JSONStoreChangeOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.api.JSONStoreFindOptions;
import com.jsonstore.api.JSONStoreInitOptions;
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.api.JSONStoreRemoveOptions;
import com.jsonstore.api.JSONStoreReplaceOptions;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class GettingStartedTests extends InstrumentationTestCase {

	final Logger logger =  Logger.getLogger("CollectionaddDataTest");

	public GettingStartedTests() {
		super();
	}

	/**
	 * @return The {@link Context} of the test project.
	 */
	private Context getTestContext()
	{
		try
		{

			return getInstrumentation().getContext();
		}
		catch (final Exception exception)
		{
			exception.printStackTrace();
			return null;
		}
	}


	public void test_a_InitAndOpen() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
			//Create the collections object that will be initialized
			JSONStoreCollection peopleCollection = new JSONStoreCollection("people");
			peopleCollection.setSearchField("name", SearchFieldType.STRING);
			peopleCollection.setSearchField("age", SearchFieldType.INTEGER);
			peopleCollection.setSearchField("id", SearchFieldType.INTEGER);
			collections.add(peopleCollection);
			
			//Optional options object
			JSONStoreInitOptions initOptions = new JSONStoreInitOptions();			
			initOptions.setUsername("carlos"); //Optional username, default 'jsonstore'
			initOptions.setPassword("123"); //Optional password, default no password
					
			//Open the collection
			JSONStore.getInstance(ctx).openCollections(collections, initOptions);
			
			//Add data to the collection
			JSONObject newDocument = new JSONObject("{name: 'carlos', age: 10}");
			JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
			addOptions.setMarkDirty(true);
			peopleCollection.addData(newDocument, addOptions);	
			
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above (init, add)
			throw ex;
		}
		catch (JSONException ex) {
		   //Handle failure for any JSON parsing issues
			throw ex;
		}	
	}
	
	public void test_c_Find() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");

			JSONStoreQueryParts findQuery = new JSONStoreQueryParts();
			JSONStoreQueryPart part = new JSONStoreQueryPart();
			part.addLike("name", "carlos");
			part.addLessThan("age", 99);
			findQuery.addQueryPart(part);
					
			
			//Add additional find options (optional)
			JSONStoreFindOptions findOptions = new JSONStoreFindOptions();
			
			findOptions.setLimit(10); //Returns a maximum of 10 documents, default no limit
			findOptions.setOffset(0); //Skip 0 documents, default no offset
			
			findOptions.addSearchFilter("_id"); //Search fields to return, default : ['_id', 'json']
			findOptions.addSearchFilter("json");

				
			//How to sort the values returned, default no sort
			findOptions.sortBySearchFieldAscending("name");
			findOptions.sortBySearchFieldDescending("age");

			//Find documents matching query
			List<JSONObject> results = peopleCollection.findDocuments(findQuery, findOptions);	
			
			assertEquals("docs", 1, results.size());
			assertEquals("name", "carlos", results.get(0).getJSONObject("json").getString("name"));
			assertEquals("age", 10, results.get(0).getJSONObject("json").getInt("age"));
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	
	}
	
	public void test_d_Replace() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
			
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
				
			//Documents will be located with their '_id' field 
			//and replaced with the data in the 'json' field 
			JSONObject replaceDoc = new JSONObject("{_id: 1, json: {name: 'carlitos', age: 99}}");
				
			//Mark data as dirty (true = yes, false = no), default true
			JSONStoreReplaceOptions replaceOptions = new JSONStoreReplaceOptions();
			replaceOptions.setMarkDirty(true);		
				
			//Replace the document	
			peopleCollection.replaceDocument(replaceDoc, replaceOptions);	
		} 
		catch (JSONStoreException ex) {
			//Handle failure for any of the JSONStore operations above
			throw ex;
		}
		catch (JSONException ex) {
			//Handle failure for any JSON parsing issues
			throw ex;
		}
	}
	
	public void test_e_Remove() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
					
			//Documents will be located with their '_id' field
			int id = 1;
				
			JSONStoreRemoveOptions removeOptions = new JSONStoreRemoveOptions();
			
			//Mark data as dirty (true = yes, false = no), default true
			removeOptions.setMarkDirty(true); 	
					
			//Replace the document	
			peopleCollection.removeDocumentById(id, removeOptions);	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_f_Count() throws Throwable {

		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Count all documents that match the query.
			JSONStoreQueryParts countQuery = new JSONStoreQueryParts();
			JSONStoreQueryPart part = new JSONStoreQueryPart();
			
			// Exact match
			part.addEqual("name", "carlos");
			countQuery.addQueryPart(part);

					
			//Replace the document	
			int resultCount = peopleCollection.countDocuments(countQuery);
			JSONObject doc = peopleCollection.findDocumentById(resultCount);
			peopleCollection.replaceDocument(doc);	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	
	}
	

	public void test_g_SecurityChangePassword() throws Throwable {
		//The password should be user input. 
		//It's hardcoded in the example for brevity.
		String username = "carlos";
		String oldPassword = "123";
		String newPassword = "456";
		
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			JSONStore.getInstance(ctx).changePassword(username, oldPassword, newPassword);
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above   
		   throw ex;
		} 
		finally {
			//It is good practice to not leave passwords in memory
			oldPassword = null;
			newPassword = null;
		}
	}
	
	public void test_h_Push() throws Throwable {

		 //Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Check if document with _id 3 is dirty
			List<JSONObject> allDirtyDocuments = peopleCollection.findAllDirtyDocuments();
			
			//Handle the dirty documents here (e.g. calling an adapter)
			
			peopleCollection.markDocumentsClean(allDirtyDocuments);	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_i_Transaction() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			JSONStore.getInstance(ctx).startTransaction();

					
			JSONObject docToAdd = new JSONObject("{name: 'carlos', age: 99}");					
			//Find documents matching query
			peopleCollection.addData(docToAdd);
			
			//Remove added doc
			int id  = 1;
			peopleCollection.removeDocumentById(id);
			
			JSONStore.getInstance(ctx).commitTransaction();	
		} 
		catch (JSONStoreException ex) {
		    //Handle failure for any of the JSONStore operations above  
		    
		    //An exception occured and we should take care of it to prevent further damage
			JSONStore.getInstance(ctx).rollbackTransaction();
		   
		   throw ex;  
		}
		catch (JSONException ex) {
		    //Handle failure for any JSON parsing issues  
		    //An exception occured and we should take care of it to prevent further damage
			JSONStore.getInstance(ctx).rollbackTransaction();

		   throw ex;
		}
	}
	
	public void test_j_Pull() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Pull data here and place in newDocs. For this example, it is hardcoded.
			List<JSONObject> newDocs = new ArrayList<JSONObject>();
			JSONObject doc = new JSONObject("{id: 1, ssn: '111-22-3333', name: 'carlos'}");
			newDocs.add(doc);
			
			JSONStoreChangeOptions changeOptions = new JSONStoreChangeOptions();
			
			//Data that doesn't exist in the collection will be added, default false
			changeOptions.setAddNew(true);	
			
			//Mark data as dirty (true = yes, false = no), default false 
			changeOptions.setMarkDirty(true);
			
			 //The following assumes that 'id' and 'ssn' are search fields, 
		    //default will use all search fields
		    //and are part of the data being received 
		    List<String> replaceCriteria = new ArrayList<String>();
		    replaceCriteria.add("id");
		    replaceCriteria.add("ssn");
			changeOptions.addSearchFieldToCriteria("id");
			  	  
			int changed = peopleCollection.changeData(newDocs, changeOptions);	
			
			assertEquals("changed doc", 1, changed);
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
		catch (JSONException ex) {
		   //Handle failure for any JSON parsing issues
		   throw ex;
		}
		
	}
	
	public void test_k_IsDirty() throws Throwable {
		
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Check if document with id '1' is dirty
			boolean isDirty = peopleCollection.isDocumentDirty(1);
			
			assertFalse(isDirty);
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_l_NumberOfDirty() throws Throwable {
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Get the count of all dirty documents in the people collection
			int totalDirty = peopleCollection.countAllDirtyDocuments();	
			
			assertEquals("dirty docs", 1, totalDirty);
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_m_ClearCollection() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Clear the collection.
			peopleCollection.clearCollection();	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_n_RemoveCollection() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Get the already initialized collection
			JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
			
			//Remove the collection. The collection object is
			//no longer usable
			peopleCollection.removeCollection();	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}
	
	public void test_o_CloseAll() throws Throwable {

		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Close access to all collections
			JSONStore.getInstance(ctx).closeAll();	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}

	}
	
	public void test_p_Destroy() throws Throwable {
		//Fill in the blank to get the Android application context
		Context ctx = getTestContext();
		
		try {
			//Destroy the store
			JSONStore.getInstance(ctx).destroy();	
		} 
		catch (JSONStoreException ex) {
		   //Handle failure for any of the JSONStore operations above
		   throw ex;
		}
	}

}


