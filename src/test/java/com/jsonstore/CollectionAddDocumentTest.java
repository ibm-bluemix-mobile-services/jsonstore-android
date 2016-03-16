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
import com.jsonstore.exceptions.JSONStoreException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.jackson.JsonOrgModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollectionAddDocumentTest extends InstrumentationTestCase {

	final Logger logger =  Logger.getLogger("CollectionaddDataTest");

	public CollectionAddDocumentTest() {
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



	public void testaddDataSimple() throws JSONException, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject obj = new JSONObject();
		obj.put("win", 123);		
		
		simpleCol.addData(obj);
		
		store.destroy();
	
	}
	
	public void testaddDataAndRemove() throws JSONException, Throwable {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONObject obj3 = new JSONObject();
		JSONObject obj4 = new JSONObject();
		
		obj1.put("name", "carlos");
		obj1.put("age", 1);
		obj2.put("name", "dgonz");
		obj2.put("age", 2);
		obj3.put("name", "mike");
		obj3.put("age", 3);
		obj4.put("name", "nana");
		obj4.put("age", 4);
		
		logger.log(Level.INFO, "Document pre-insert: " + obj1.toString());
		simpleCol.addData(obj1);
		logger.log(Level.INFO, "Document pre-insert: " + obj2.toString());
		simpleCol.addData(obj2);
		logger.log(Level.INFO, "Document pre-insert: " + obj3.toString());
		simpleCol.addData(obj3);
		logger.log(Level.INFO, "Document pre-insert: " + obj4.toString());
		simpleCol.addData(obj4);
				
		
		List <JSONObject> results = simpleCol.findAllDocuments();
		
		obj1 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		obj2 = JsonOrgModule.deserializeJSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		obj3 = JsonOrgModule.deserializeJSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		obj4 = JsonOrgModule.deserializeJSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));

		
		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 4", 4, simpleCol.countAllDocuments());
		assertEquals("name1", "carlos", obj1.getString("name"));
		assertEquals("age1", 1, obj1.getInt("age"));
		assertEquals("name2", "dgonz", obj2.getString("name"));
		assertEquals("age2", 2, obj2.getInt("age"));
		assertEquals("name3", "mike", obj3.getString("name"));
		assertEquals("age3", 3, obj3.getInt("age"));
		assertEquals("name4", "nana", obj4.getString("name"));
		assertEquals("age4", 4, obj4.getInt("age"));
		
		simpleCol.removeDocumentById(results.get(0).getInt(DatabaseConstants.FIELD_ID));
		
		
		results = simpleCol.findAllDocuments();
		
		obj2 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		obj3 = JsonOrgModule.deserializeJSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		obj4 = JsonOrgModule.deserializeJSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));

		
		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 3", 3, simpleCol.countAllDocuments());
		assertEquals("name2", "dgonz", obj2.getString("name"));
		assertEquals("age2", 2, obj2.getInt("age"));
		assertEquals("name3", "mike", obj3.getString("name"));
		assertEquals("age3", 3, obj3.getInt("age"));
		assertEquals("name4", "nana", obj4.getString("name"));
		assertEquals("age4", 4, obj4.getInt("age"));
		
		store.destroy();
		
	}
	
	
	public void testaddDataUsingAdditionalSearchFields() throws JSONStoreException, Throwable {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("ssn", SearchFieldType.INTEGER);
		
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONObject obj3 = new JSONObject();
		JSONObject obj4 = new JSONObject();
		
		obj1.put("name", "carlos");
		obj1.put("age", 1);
		obj1.put("ssn", 123);
		obj2.put("name", "dgonz");
		obj2.put("age", 2);
		obj2.put("ssn", 345);
		obj3.put("name", "mike");
		obj3.put("age", 3);
		obj3.put("ssn", 567);
		obj4.put("name", "nana");
		obj4.put("age", 4);
		obj4.put("ssn", 789);
		
		logger.log(Level.INFO, "Document pre-insert: " + obj1.toString());
		simpleCol.addData(obj1);
		logger.log(Level.INFO, "Document pre-insert: " + obj2.toString());
		simpleCol.addData(obj2);
		logger.log(Level.INFO, "Document pre-insert: " + obj3.toString());
		simpleCol.addData(obj3);
		logger.log(Level.INFO, "Document pre-insert: " + obj4.toString());
		simpleCol.addData(obj4);
			
		
		List <JSONObject> results = simpleCol.findAllDocuments();
		
		obj1 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		obj2 = JsonOrgModule.deserializeJSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		obj3 = JsonOrgModule.deserializeJSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		obj4 = JsonOrgModule.deserializeJSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));


		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 4", 4, simpleCol.countAllDocuments());
		assertEquals("name1", "carlos", obj1.getString("name"));
		assertEquals("age1", 1, obj1.getInt("age"));
		assertEquals("ssn1", 123, obj1.getInt("ssn"));
		assertEquals("name2", "dgonz", obj2.getString("name"));
		assertEquals("age2", 2, obj2.getInt("age"));
		assertEquals("ssn2", 345, obj2.getInt("ssn"));
		assertEquals("name3", "mike", obj3.getString("name"));
		assertEquals("age3", 3, obj3.getInt("age"));
		assertEquals("ssn3", 567, obj3.getInt("ssn"));
		assertEquals("name4", "nana", obj4.getString("name"));
		assertEquals("age4", 4, obj4.getInt("age"));
		assertEquals("ssn4", 789, obj4.getInt("ssn"));
		
		store.destroy();
			
	}
	
	
	public void testaddDataWithSymbol() throws JSONStoreException, Throwable {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("[name;", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("ssn", SearchFieldType.INTEGER);
		
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONObject obj3 = new JSONObject();
		JSONObject obj4 = new JSONObject();
		
		obj1.put("[name;", "carlos");
		obj1.put("age", 1);
		obj2.put("[name;", "dgonz");
		obj2.put("age", 2);
		obj3.put("[name;", "mike");
		obj3.put("age", 3);
		obj4.put("[name;", "nana");
		obj4.put("age", 4);
		
		logger.log(Level.INFO, "Document pre-insert: " + obj1.toString());
		simpleCol.addData(obj1);
		logger.log(Level.INFO, "Document pre-insert: " + obj2.toString());
		simpleCol.addData(obj2);
		logger.log(Level.INFO, "Document pre-insert: " + obj3.toString());
		simpleCol.addData(obj3);
		logger.log(Level.INFO, "Document pre-insert: " + obj4.toString());
		simpleCol.addData(obj4);
		
		List <JSONObject> results = simpleCol.findAllDocuments();


		obj1 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		obj2 = JsonOrgModule.deserializeJSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		obj3 = JsonOrgModule.deserializeJSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		obj4 = JsonOrgModule.deserializeJSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));

		
		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 4", 4, simpleCol.countAllDocuments());
		assertEquals("name1", "carlos", obj1.getString("[name;"));
		assertEquals("age1", 1, obj1.getInt("age"));
		assertEquals("name2", "dgonz", obj2.getString("[name;"));
		assertEquals("age2", 2, obj2.getInt("age"));
		assertEquals("name3", "mike", obj3.getString("[name;"));
		assertEquals("age3", 3, obj3.getInt("age"));
		assertEquals("name4", "nana", obj4.getString("[name;"));
		assertEquals("age4", 4, obj4.getInt("age"));
		
		store.destroy();
		
	}
	
	public void testaddDataWithNestedSearchField() throws JSONStoreException, Throwable {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setSearchField("order.name", SearchFieldType.STRING);
		
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		
		JSONObject obj1 = new JSONObject();
		JSONObject obj2 = new JSONObject();
		JSONArray jArray = new JSONArray();
		JSONObject nestedData1 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		obj1.put("name", "carlos");
		obj1.put("age", 1);
		nestedData1.put("name", "hello");
		nestedData2.put("name", "hey");
		nestedData3.put("name", "hola");
		jArray.put(nestedData1);
		jArray.put(nestedData2);
		jArray.put(nestedData3);
		obj1.put("order", jArray);
		
		nestedData1 = new JSONObject();
		nestedData2 = new JSONObject();
		nestedData3 = new JSONObject();
		
		obj2.put("name", "dgonz");
		obj2.put("age", 2);
		nestedData1.put("name", "hello");
		nestedData2.put("name", "hey");
		nestedData3.put("name", "hola");
		jArray.put(nestedData1);
		jArray.put(nestedData2);
		jArray.put(nestedData3);
		obj2.put("order", jArray);
		
		
		logger.log(Level.INFO, "Document pre-insert: " + obj1.toString());
		simpleCol.addData(obj1);
		logger.log(Level.INFO, "Document pre-insert: " + obj2.toString());
		simpleCol.addData(obj2);
		
		
		List <JSONObject> results = simpleCol.findAllDocuments();


		obj1 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		obj2 = JsonOrgModule.deserializeJSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 2", 2, simpleCol.countAllDocuments());
		assertEquals("name1", "carlos", obj1.getString("name"));
		assertEquals("age1", 1, obj1.getInt("age"));
		//TODO: nana - make it so that array list returns a string in the below format
		//assertEquals("order.name1", "hello-@-hey-@-hola", results.get(0).get("order.name"));
		assertEquals("name2", "dgonz", obj2.getString("name"));
		assertEquals("age2", 2, obj2.getInt("age"));
		//TODO: nana - make it so that array list returns a string in the below format
		//assertEquals("order.name2", "hello-@-hey-@-hola", results.get(1).get("order.name"));
		
		store.destroy();
		
	}
	
	public void testAddEmptyDocument() throws Throwable, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		

		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		
		JSONObject obj1 = new JSONObject();
		
		logger.log(Level.INFO, "Document pre-insert: " + obj1.toString());
		simpleCol.addData(obj1);
		
		
		List <JSONObject> results = simpleCol.findAllDocuments();
		obj1 = JsonOrgModule.deserializeJSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		
		

		assertEquals("Number of docs is " + simpleCol.countAllDocuments() + " should be 1", 1, simpleCol.countAllDocuments());
		assertEquals("Number of discovered docs shouldn't be greater than 1", 1, results.size());
		assertEquals("empty json", 0, obj1.length());
		
	}
	
	public void testInvalidAddWithJSONID() throws Throwable, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreInvalidSchemaException err = null;
		store.destroy();
		
		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setSearchField("_id", SearchFieldType.INTEGER);
		

		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		
		try
		{
			store.openCollections(collections);
		} catch(JSONStoreInvalidSchemaException e) {
			err = e;
		}
		
		
		assertNotNull(err);	

	}

}
