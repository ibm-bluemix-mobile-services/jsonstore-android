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

package com.jsonstore;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.jsonstore.api.JSONStore;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreNoTransactionInProgressException;
import com.jsonstore.exceptions.JSONStoreTransactionDuringInitException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;
import com.jsonstore.exceptions.JSONStoreTransactionInProgressException;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionTransactionTest extends InstrumentationTestCase {

	
	public CollectionTransactionTest() {
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


	public void testStartAddRemoveCommitTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		store.startTransaction();
		
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
	
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("docs1", 3, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1" ,1, data1.get("age"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age")); 
		
		col.removeDocumentById(results.get(0).getInt(DatabaseConstants.FIELD_ID));
		
		results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("docs2", 2, col.countAllDocuments());
		
		assertEquals("name2", "dgonz", data1.get("name"));
		assertEquals("age2", 2, data1.get("age"));
		assertEquals("name3", "mike", data2.get("name"));
		assertEquals("age3", 3, data2.get("age")); 
		
		assertEquals("commitTransaction", true, store.commitTransaction());
		
		results = col.findAllDocuments();	
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals("docs3", 2, col.countAllDocuments());
		assertEquals("name2", "dgonz", data1.get("name"));
		assertEquals("age2", 2, data1.get("age"));
		assertEquals("name3", "mike", data2.get("name"));
		assertEquals("age3", 3, data2.get("age")); 
		
	}
	
	public void testStartAddRemoveRollbackTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		store.startTransaction();
		
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
	
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("docs1", 3, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1" ,1,  data1.get("age"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age")); 
		
		assertEquals("rollbackTransaction", true, store.rollbackTransaction());
		
		assertEquals("docs2", 0, col.countAllDocuments());
	
	}
	
	public void testTransactionDBClosed() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.closeAll();
		
		try{
			store.startTransaction();
		} catch(JSONStoreDatabaseClosedException e){
			err = e;
		}
		
		assertNotNull(err);
	}
	
	public void testCommitTransactionDBClosed() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.closeAll();
		
		try{
			store.commitTransaction();
		} catch(JSONStoreDatabaseClosedException e){
			err = e;
		}
		
		assertNotNull(err);
	}
	
	public void testRollbackTransactionDBClosed() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.closeAll();
		
		try{
			store.rollbackTransaction();
		} catch(JSONStoreDatabaseClosedException e){
			err = e;
		}
		
		assertNotNull(err);
	}
	
	public void testCallInitDuringTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreTransactionDuringInitException err = null;
		store.destroy();
		JSONStoreCollection col1 = new JSONStoreCollection("customers");
		JSONStoreCollection col2 = new JSONStoreCollection("orders");
		col1.setSearchField("name", SearchFieldType.STRING);
		col1.setSearchField("age", SearchFieldType.INTEGER);
		col2.setSearchField("name", SearchFieldType.STRING);
		col2.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection 1
		List<JSONStoreCollection> collection_set1 = new LinkedList<JSONStoreCollection>();
		collection_set1.add(col1);
		store.openCollections(collection_set1);
		
		store.startTransaction();
		
		List<JSONStoreCollection> collection_set2 = new LinkedList<JSONStoreCollection>();
		collection_set2.add(col2);
		
		try {
			store.openCollections(collection_set2);
		} catch(JSONStoreTransactionDuringInitException e)
		{
			err = e;
		} finally {
			store.rollbackTransaction();
		}
		
		assertNotNull(err);
		
	}
	
	public void testStartTransactionTwice() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreTransactionInProgressException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		
		try {
			store.startTransaction();
			store.startTransaction();
		}catch(JSONStoreTransactionInProgressException e)
		{
			err = e;
		} finally {
			store.rollbackTransaction();
		}
		
		assertNotNull(err);	
	}
	
	public void testCommitWOTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreNoTransactionInProgressException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		
		try {
			store.commitTransaction();
		}catch(JSONStoreNoTransactionInProgressException e)
		{
			err = e;
		}
		
		assertNotNull(err);	
	}
	
	public void testRollbackWOTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreNoTransactionInProgressException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		
		try {
			store.rollbackTransaction();
		}catch(JSONStoreNoTransactionInProgressException e)
		{
			err = e;
		}
		
		assertNotNull(err);	
	}
	
	public void testDBCloseDuringTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreTransactionFailureException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.startTransaction();
		
		try {
			store.closeAll();
		} catch(JSONStoreTransactionFailureException e) {
			err = e;
		} finally {
			store.rollbackTransaction();
		}
		
		assertNotNull(err);
	}
	
	public void testDestroyDBDuringTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreTransactionFailureException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.startTransaction();
		
		try {
			store.destroy();
		} catch(JSONStoreTransactionFailureException e) {
			err = e;
		} finally {
			store.rollbackTransaction();
		}
		
		assertNotNull(err);
	}
	
	public void testRemoveCollectionDuringTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreTransactionFailureException err = null;
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		store.startTransaction();
		
		try {
			col.removeCollection();
		} catch(JSONStoreTransactionFailureException e) {
			err = e;
			
		}finally {
			store.rollbackTransaction();
		}
		
		assertNotNull(err);
	}
	
	public void testClearCollectionDuringTransaction() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		store.startTransaction();
		
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
	
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		
		assertEquals("docs1", 3, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1" , 1, data1.get("age"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age")); 
		
		col.clearCollection();
		
		assertEquals("docs2", 0, col.countAllDocuments());
		
		
		assertEquals("commitTransaction", true, store.commitTransaction());
		
		assertEquals("docs3", 0, col.countAllDocuments());
				
	}
	
	public void testMulipleCollecitonTranscations() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col1 = new JSONStoreCollection("customers");
		JSONStoreCollection col2 = new JSONStoreCollection("orders");
		col1.setSearchField("name", SearchFieldType.STRING);
		col1.setSearchField("age", SearchFieldType.INTEGER);
		col2.setSearchField("name", SearchFieldType.STRING);
		col2.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection 1
		List<JSONStoreCollection> collection = new LinkedList<JSONStoreCollection>();
		collection.add(col1);
		collection.add(col2);
		store.openCollections(collection);
		
		JSONObject data1a = new JSONObject();
		JSONObject data2a = new JSONObject();
		JSONObject data3a = new JSONObject();
		JSONObject data1b = new JSONObject();
		JSONObject data2b = new JSONObject();
		JSONObject data3b = new JSONObject();
		
		
		data1a.put("name", "carlos");
		data1a.put("age", 1);
		data2a.put("name", "dgonz");
		data2a.put("age", 2);
		data3a.put("name", "mike");
		data3a.put("age", 3);
		
		data1b.put("name", "Apple");
		data1b.put("id", 1);
		data2b.put("name", "Windows");
		data2b.put("id", 2);
		data3b.put("name", "Linux");
		data3b.put("id", 3);
		
		store.startTransaction();
		
		col1.addData(data1a);
		col1.addData(data2a);
		col1.addData(data3a);
		col2.addData(data1b);
		col2.addData(data2b);
		col2.addData(data3b);
		
		List<JSONObject> results1 = col1.findAllDocuments();
		
		data1a = new JSONObject(results1.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2a = new JSONObject(results1.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3a = new JSONObject(results1.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs1", 3, col1.countAllDocuments());
		assertEquals("name1", "carlos", data1a.get("name"));
		assertEquals("age1", 1, data1a.get("age"));
		assertEquals("name2", "dgonz", data2a.get("name"));
		assertEquals("age2", 2, data2a.get("age"));
		assertEquals("name3", "mike", data3a.get("name"));
		assertEquals("age3", 3, data3a.get("age"));
		
		List<JSONObject> results2 = col2.findAllDocuments();
		
		data1b = new JSONObject(results2.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2b = new JSONObject(results2.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3b = new JSONObject(results2.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs1", 3, col2.countAllDocuments());
		assertEquals("name1", "Apple", data1b.get("name"));
		assertEquals("id1", 1, data1b.get("id"));
		assertEquals("name2", "Windows", data2b.get("name"));
		assertEquals("id2", 2, data2b.get("id"));
		assertEquals("name3", "Linux", data3b.get("name"));
		assertEquals("id3", 3, data3b.get("id"));

		col1.removeDocumentById(results2.get(0).getInt(DatabaseConstants.FIELD_ID));
		
		col2.clearCollection();
		
		assertEquals("commitTransaction" , true, store.commitTransaction());
		
		assertEquals("docs1", 2, col1.countAllDocuments());
		assertEquals("docs2", 0, col2.countAllDocuments());	
	}
	
	public void testAddOperationBeforeTransactionAndRollback() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection 1
		List<JSONStoreCollection> collection = new LinkedList<JSONStoreCollection>();
		collection.add(col);
		store.openCollections(collection);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		col.addData(data1);
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
	
		assertEquals("docs", 1, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		
		store.startTransaction();
		
		col.addData(data2);
		
		results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("docs", 2, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		
		assertEquals("rollbackTransaction", true, store.rollbackTransaction());
		
		results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("docs", 1, col.countAllDocuments());
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
			
	}
	
	
	
	
}
