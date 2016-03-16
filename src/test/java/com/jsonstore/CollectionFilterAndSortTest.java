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
import com.jsonstore.api.JSONStoreAddOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.api.JSONStoreFindOptions;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionFilterAndSortTest extends InstrumentationTestCase {
	
	public CollectionFilterAndSortTest()
	{
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

	
	public void testfilterByProvideSearchFieldName() throws Exception{
	JSONStore store = JSONStore.getInstance(getTestContext());
	store.destroy();
	JSONStoreCollection col = new JSONStoreCollection("simple");
	col.setSearchField("name", SearchFieldType.STRING);
	col.setSearchField("age", SearchFieldType.INTEGER);
	
	//Open collection.
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
	data3.put("name", "nana");
	data3.put("age", 3);
	
	col.addData(data1);
	col.addData(data2);
	col.addData(data3);
	
	
	JSONStoreFindOptions opt = new JSONStoreFindOptions();
	opt.addSearchFilter("name");
	List <JSONObject> results = col.findAllDocuments(opt);
	
	assertEquals(3, results.size());
	assertEquals("carlos", results.get(0).get("name"));
	assertEquals(1, results.get(0).length());
	assertEquals("dgonz", results.get(1).get("name"));
	assertEquals(1, results.get(1).length());
	assertEquals("nana", results.get(2).get("name"));
	assertEquals(1, results.get(2).length());	
	
}

	public void testFilterByFieldID() throws Exception { 
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter(DatabaseConstants.FIELD_ID);
		List <JSONObject> results = col.findAllDocuments(opt);
		
		assertEquals(3, results.size());
		assertEquals(1, results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals(1, results.get(0).length(), 1);
		assertEquals(2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals(1, results.get(1).length());
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals(1, results.get(2).length());
		
		store.destroy();
		
	}
	
	public void testFilerByFieldIDJSON() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter(DatabaseConstants.FIELD_JSON);
		opt.addSearchFilter(DatabaseConstants.FIELD_ID);
		List <JSONObject> results = col.findAllDocuments(opt);
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals(1,data1.get("age"));
		assertEquals("carlos", data1.get("name"));
		assertEquals(2, results.get(0).length());
		 
		assertEquals(2,results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("dgonz", data2.get("name"));
		assertEquals(2, data2.get("age"));
		assertEquals(2, results.get(1).length());
		
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("nana", data3.get("name"));
		assertEquals(3, data3.get("age"));
		assertEquals(2, results.get(2).length());
		
		store.destroy();
		
	}
	
	public void testFilterByFieldIDJSONAndSF() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter(DatabaseConstants.FIELD_JSON);
		opt.addSearchFilter(DatabaseConstants.FIELD_ID);
		opt.addSearchFilter("age");
		List <JSONObject> results = col.findAllDocuments(opt);
		
		
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals(1,data1.get("age"));
		assertEquals("carlos", data1.get("name"));
		assertEquals("1", results.get(0).get("age"));
		assertEquals(3, results.get(0).length());
		 
		assertEquals(2,results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("dgonz", data2.get("name"));
		assertEquals(2, data2.get("age"));
		assertEquals("2", results.get(1).get("age"));
		assertEquals(3, results.get(1).length());
		
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("nana", data3.get("name"));
		assertEquals(3, data3.get("age"));
		assertEquals("3", results.get(2).get("age"));
		assertEquals(3, results.get(2).length());
		
		store.destroy();
	
	}
	
	public void testFilterByAllDatabaseConstants() throws Exception{
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter(DatabaseConstants.FIELD_JSON);
		opt.addSearchFilter(DatabaseConstants.FIELD_ID);
		opt.addSearchFilter(DatabaseConstants.FIELD_DELETED);
		opt.addSearchFilter(DatabaseConstants.FIELD_DIRTY);
		opt.addSearchFilter(DatabaseConstants.FIELD_OPERATION);
		List <JSONObject> results = col.findAllDocuments(opt);
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals(1,data1.get("age"));
		assertEquals("carlos", data1.get("name"));
		assertNotNull(results.get(0).get(DatabaseConstants.FIELD_DIRTY));
		assertEquals("0", results.get(0).get(DatabaseConstants.FIELD_DELETED));
		assertEquals("store", results.get(0).get(DatabaseConstants.FIELD_OPERATION));
		assertEquals(5, results.get(0).length());
		 
		assertEquals(2,results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("dgonz", data2.get("name"));
		assertEquals(2, data2.get("age"));
		assertNotNull(results.get(1).get(DatabaseConstants.FIELD_DIRTY));
		assertEquals("0", results.get(1).get(DatabaseConstants.FIELD_DELETED));
		assertEquals("store", results.get(1).get(DatabaseConstants.FIELD_OPERATION));
		assertEquals(5, results.get(1).length());
		
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("nana", data3.get("name"));
		assertEquals(3, data3.get("age"));
		assertNotNull(results.get(2).get(DatabaseConstants.FIELD_DIRTY));
		assertEquals("0", results.get(2).get(DatabaseConstants.FIELD_DELETED));
		assertEquals("store", results.get(2).get(DatabaseConstants.FIELD_OPERATION));
		assertEquals(5, results.get(2).length());
		
		store.destroy();
		
	}
	
	public void testFilterByAdditionalSearchField() throws Exception{
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(data1, optAdd);
		col.addData(data2, optAdd);
		col.addData(data3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("lol");
		List <JSONObject> results = col.findAllDocuments(opt);
				
		assertEquals("docs", 1, results.size());
		assertEquals("hey", results.get(0).get("lol"));
		assertEquals(1, results.get(0).length());
		
		store.destroy();
	}
	
	public void testFilterDuplicateData() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setSearchField("22age", SearchFieldType.INTEGER);
		col.setSearchField("namel", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject("{\"name\": \"carlos\", \"namel\": \"anderu\", \"22age\": 3, \"age\": 1}");
		JSONObject data3 = new JSONObject();
		
		data1.put("namel", "anderu");
		data1.put("name", "carlos");
		data1.put("22age", 3);
		data1.put("age", 1);
		data3.put("name", "nana");
		data3.put("namel", "amfo");
		data3.put("22age", 9);
		data3.put("age", 3);
	
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(data1, optAdd);
		col.addData(data2, optAdd);
		col.addData(data3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("lol");
		opt.addSearchFilter("name");
		opt.addSearchFilter("namel");
		List <JSONObject> results = col.findAllDocuments(opt);		
		

		assertEquals("docs", 2, results.size());
		assertEquals("hey", results.get(0).get("lol"));
		assertEquals("anderu", results.get(0).get("namel"));
		assertEquals("carlos", results.get(0).get("name"));
		assertEquals("hey", results.get(1).get("lol"));
		assertEquals("amfo", results.get(1).get("namel"));
		assertEquals("nana", results.get(1).get("name"));
		

	}
	
	public void testFilterByFieldIDAndAdditionalSearchField() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
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
		data3.put("name", "nana");
		data3.put("age", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(data1, optAdd);
		col.addData(data2, optAdd);
		col.addData(data3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter(DatabaseConstants.FIELD_ID);
		opt.addSearchFilter("lol");
		List <JSONObject> results = col.findAllDocuments(opt);			
		
		assertEquals(3, results.size());
		assertEquals("hey", results.get(0).get("lol"));
		assertEquals(1, results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals(2, results.get(0).length());
		assertEquals("hey", results.get(1).get("lol"));
		assertEquals(2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals(2, results.get(1).length());
		assertEquals("hey", results.get(2).get("lol"));
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals(2, results.get(2).length());
		
		store.destroy();
	}
	
	public void testFilterByNestedSearchFields() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("age", 1);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("age", 2);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("age", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("name.first");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(results.size(), 3);
		assertEquals("carlos", results.get(0).get("name.first"));
		assertEquals(1, results.get(0).length());
		assertEquals("dgonz", results.get(1).get("name.first"));
		assertEquals(1, results.get(1).length());
		assertEquals("nana", results.get(2).get("name.first"));
		assertEquals(1, results.get(2).length());	
		
		store.destroy();
	}
	
	public void testFilterBySearchFieldNumber() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("22age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("22age", 1);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("22age", 2);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("22age", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("22age");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(3, results.size());
		assertEquals("1", results.get(0).get("22age"));
		assertEquals(1, results.get(0).length());
		assertEquals("2", results.get(1).get("22age"));
		assertEquals(1, results.get(1).length());
		assertEquals("3", results.get(2).get("22age"));
		assertEquals(1, results.get(2).length());
		
		store.destroy();
	}
	
	public void testGVTSearchField() throws Exception{
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("地震に関する編");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(3, results.size());
		assertEquals("1", results.get(0).get("地震に関する編"));
		assertEquals(1, results.get(0).length());
		assertEquals("2", results.get(1).get("地震に関する編"));
		assertEquals(1, results.get(1).length());
		assertEquals("3", results.get(2).get("地震に関する編"));
		assertEquals(1, results.get(2).length());
		
		store.destroy();
	}
	
	public void testFilterWithLimit() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		nestedData1.put("bool", true);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		nestedData2.put("bool", false);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);
		nestedData3.put("bool", true);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("地震に関する編");
		opt.setLimit(1);
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		assertEquals(1, results.size());
		assertEquals("1", results.get(0).get("地震に関する編"));
		assertEquals(1, results.get(0).length());
		
		store.destroy();
	
	}
	
	public void testFilterByLimitAndSort() throws Exception{
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		nestedData1.put("bool", true);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		nestedData2.put("bool", false);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);
		nestedData3.put("bool", true);

		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("name.first");
		opt.addSearchFilter("bool");
		opt.sortBySearchFieldDescending("name.first");
		opt.setLimit(2);
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		assertEquals(results.size(), 2);
		assertEquals("nana", results.get(0).get("name.first"));
		assertEquals("1", results.get(0).get("bool"));
		assertEquals(2, results.get(0).length());
		assertEquals("dgonz", results.get(1).get("name.first"));
		assertEquals("0", results.get(1).get("bool"));
		assertEquals(2, results.get(1).length());
		
		store.destroy();
	}
	
	public void testFilterByLimitSortAndNestedSearchField() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		nestedData1.put("bool", true);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		nestedData2.put("bool", false);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);
		nestedData3.put("bool", true);
	
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("name.first");
		opt.addSearchFilter("bool");
		opt.sortBySearchFieldDescending("name_first");
		opt.setLimit(2);
		opt.setOffset(1);
		
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		assertEquals(results.size(), 2);
		assertEquals(results.get(0).get("name.first"),  "dgonz");
		assertEquals(results.get(0).get("bool"), "0");
		assertEquals(results.get(0).length(), 2);
		assertEquals("carlos", results.get(1).get("name.first"));
		assertEquals("1", results.get(1).get("bool"));
		assertEquals(2, results.get(1).length());
		
		store.destroy();
		
	}
	
	public void testFilterByEmptyCollection() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("bool");
		opt.sortBySearchFieldDescending("name.first");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		assertEquals(0, results.size());
		
		store.destroy();
		
	}
	
	public void testFilterByNestedSearchFieldAndSort() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		nestedData1.put("bool", true);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		nestedData2.put("bool", false);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);
		nestedData3.put("bool", true);
		
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		col.addData(nestedData3, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("name.first");
		opt.sortBySearchFieldDescending("name.first");
		opt.setOffset(1);
		opt.setLimit(2);
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(2, results.size());
		assertEquals("dgonz", results.get(0).get("name.first"));
		assertEquals(1, results.get(0).length());
		assertEquals("carlos", results.get(1).get("name.first"));
		assertEquals(1, results.get(1).length());
		
		store.destroy();
	}
	
	public void testFilterMSearchFieldMAdditionalSearchFieldAndSort() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("name.first", SearchFieldType.STRING);
		col.setSearchField("地震に関する編", SearchFieldType.INTEGER);
		col.setSearchField("bool", SearchFieldType.BOOLEAN);
		col.setAdditionalSearchField("lol", SearchFieldType.STRING);
		col.setAdditionalSearchField("noob", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject nestedData1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject nestedData3 = new JSONObject();
		
		data1.put("first", "carlos");
		nestedData1.put("name", data1);
		nestedData1.put("地震に関する編", 1);
		nestedData1.put("bool", true);
		data2.put("first", "dgonz");
		nestedData2.put("name", data2);
		nestedData2.put("地震に関する編", 2);
		nestedData2.put("bool", false);
		data3.put("first", "nana");
		nestedData3.put("name", data3);
		nestedData3.put("地震に関する編", 3);
		nestedData3.put("bool", true);
	
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		optAdd.addAdditionalSearchField("lol","hey");
		optAdd.addAdditionalSearchField("noob", "fine");

		col.addData(nestedData1,optAdd);
		col.addData(nestedData2,optAdd);
		col.addData(nestedData3,optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("noob");
		opt.addSearchFilter("lol");
		opt.sortBySearchFieldDescending("name.first");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(1, results.size());
		assertEquals("hey", results.get(0).get("lol"));
		assertEquals("fine", results.get(0).get("noob"));
		
		store.destroy();
	
	}
	
	public void testFilterByPassSymbol() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("[name;", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
	
		
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("[name;", "carlos");
		data1.put("age", 1);
		data2.put("[name;", "dgonz");
		data2.put("age", 2);
		data3.put("[name;", "mike");
		data3.put("age", 3);
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
				

		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("[name;");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		assertEquals(3, results.size());
		assertEquals("carlos", results.get(0).get("[name;"));
		assertEquals(1, results.get(0).length());
		assertEquals("dgonz", results.get(1).get("[name;"));
		assertEquals(1, results.get(1).length());
		assertEquals("mike", results.get(2).get("[name;"));
		assertEquals(1, results.get(2).length());		
		
		store.destroy();
	}
	
	public void testFilterByNestedIndexing() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("order.name", SearchFieldType.STRING);
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject nestedData2 = new JSONObject();
		JSONObject data3 = new JSONObject();
	 	JSONArray nestedData1 = new JSONArray();
		
		data1.put("name", "hey");
		data2.put("name", "hello");
		data3.put("name","hola");
		nestedData1.put(data1);
		nestedData1.put(data2);
		nestedData1.put(data3);
		nestedData2.put("order", nestedData1);
		nestedData2.put("name", "carlos");
		nestedData2.put("age", 1);
		
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		
		col.addData(nestedData2, optAdd);
		
		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("order.name");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(1, results.size());
		assertEquals("hey-@-hello-@-hola", results.get(0).get("order.name"));
		
		store.destroy();
	
	}
	
	public void testFilterByNestedIndexingMoreData() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		
		col.setSearchField("order.name", SearchFieldType.STRING);
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
			
		
		
		JSONObject data1a = new JSONObject();
		JSONObject data2a = new JSONObject();
		JSONObject data3a = new JSONObject();
		JSONObject data1b = new JSONObject();
		JSONObject data2b = new JSONObject();
		JSONObject data3b = new JSONObject();
		
		JSONArray nestedData2a = new JSONArray();
		JSONObject nestedData2 = new JSONObject();
		
	 	JSONObject nestedData1 = new JSONObject();
	 	JSONArray nestedData1a = new JSONArray();
		
		data1a.put("name", "hey");
		data2a.put("name", "hello");
		data3a.put("name","hola");
		nestedData1a.put(data1a);
		nestedData1a.put(data2a);
		nestedData1a.put(data3a);
		nestedData1.put("order", nestedData1a);
		nestedData1.put("name", "carlos");
		nestedData2.put("age", 1);
		
		data1b.put("name", "uno");
		data2b.put("name", "dos");
		data3b.put("name", "tres");
		nestedData2a.put(data1b);
		nestedData2a.put(data2b);
		nestedData2a.put(data3b);
		nestedData2.put("order", nestedData2a);
		nestedData2.put("name", "nana");
		nestedData2.put("age", 2);
		
		
		JSONStoreAddOptions optAdd = new JSONStoreAddOptions();
		
		col.addData(nestedData1, optAdd);
		col.addData(nestedData2, optAdd);
		

		JSONStoreFindOptions opt = new JSONStoreFindOptions();
		opt.addSearchFilter("order.name");
		List <JSONObject> results = col.findAllDocuments(opt);	
		
		
		assertEquals(2, results.size());
		assertEquals("hey-@-hello-@-hola", results.get(0).get("order.name"));
		assertEquals("uno-@-dos-@-tres", results.get(1).get("order.name"));
		
		store.destroy();
	}

}
