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
import com.jsonstore.api.JSONStoreChangeOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionChangeTest extends InstrumentationTestCase {

	
	public CollectionChangeTest() {
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

	public void testSimpleChange() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		List<JSONObject> newDocuments = new LinkedList<JSONObject>();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject data4 = new JSONObject();
		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		data4.put("id", 5);
		data4.put("name", "HEYOO");
		data4.put("age", 4);
		
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id1", 1, data1.getInt("id"));
		assertEquals("name1", "carlos", data1.getString("name"));
		assertEquals("age1", 1, data1.getInt("age"));
		assertEquals(2, results.get(1).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id2", 2, data2.getInt("id"));
		assertEquals("name2", "dgonz", data2.getString("name"));
		assertEquals("age2", 2, data2.getInt("age"));
		assertEquals(3, results.get(2).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id3", 3, data3.getInt("id"));
		assertEquals("name3", "mike", data3.getString("name"));
		assertEquals("age3", 3, data3.getInt("age"));
		
		
		data1 = new JSONObject();
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2 = new JSONObject();
		data2.put("id", 2);
		data2.put("name", "#dgonz");
		data2.put("age", 2);
		
		data3 = new JSONObject();
		data3.put("id", 3);
		data3.put("name", "michael");
		data3.put("age", 5);
		
		newDocuments.add(data1);
		newDocuments.add(data2);
		newDocuments.add(data3);
		newDocuments.add(data4);
		
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", true);
		changeOptions.put("markDirty", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(4, col.changeData(newDocuments, opt));
		
		results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		data4 = new JSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals(4, results.size());
		
		assertEquals("_id1", 1, results.get(0).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("_id2", 2, results.get(1).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("_id3", 3, results.get(2).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("_id4", 4, results.get(3).getInt(DatabaseConstants.FIELD_ID));
		
		assertEquals("name1", "carlos", data1.getString("name"));
		assertEquals("name2","#dgonz", data2.getString("name"));
		assertEquals("name3", "michael", data3.getString("name"));
		assertEquals("name4", "HEYOO", data4.getString("name"));
		
		assertEquals("age1", 1, data1.getInt("age"));
		assertEquals("age2", 2, data2.getInt("age"));
		assertEquals("age3", 5, data3.getInt("age"));
		assertEquals("age4", 4, data4.getInt("age"));
		
		assertEquals("id1", 1, data1.getInt("id"));
		assertEquals("id2", 2, data2.getInt("id"));
		assertEquals("id3", 3, data3.getInt("id"));
		assertEquals("id4", 5, data4.getInt("id"));

	}
	
	public void testWithMulitpleReplaceCriteria() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		List<JSONObject> newDocuments = new LinkedList<JSONObject>();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		col.setSearchField("ssn", SearchFieldType.STRING);
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("active", SearchFieldType.BOOLEAN);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject data4 = new JSONObject();
		
		data1.put("id", 0);
		data1.put("name", "YOLO");
		data1.put("age", 10);
		data1.put("gpa", 3.8);
		data1.put("active", true);
		data1.put("ssn", "111-22-3333");
		
		data2.put("id", 1);
		data2.put("name", "mike");
		data2.put("age", 11);
		data2.put("gpa", 3.9);
		data2.put("active", false);
		data2.put("ssn", "111-44-3333");
		
		data3.put("id", 2);
		data3.put("name", "dgonz");
		data3.put("age", 12);
		data3.put("gpa", 3.7);
		data3.put("active", true);
		data3.put("ssn", "111-55-3333");
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id1", 0, data1.getInt("id"));
		assertEquals("name1", "YOLO", data1.getString("name"));
		assertEquals("age1", 10, data1.getInt("age"));
		assertEquals("gpa1", 3.8, data1.getDouble("gpa"));
		assertEquals("active1", true, data1.getBoolean("active"));
		assertEquals("ssn1", "111-22-3333", data1.getString("ssn"));
		
		assertEquals(2, results.get(1).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id2", 1, data2.getInt("id"));
		assertEquals("name2", "mike", data2.getString("name"));
		assertEquals("age2", 11, data2.getInt("age"));
		assertEquals("gpa2", 3.9, data2.getDouble("gpa"));
		assertEquals("active2", false, data2.getBoolean("active"));
		assertEquals("ssn2", "111-44-3333", data2.getString("ssn"));
		
		assertEquals(3, results.get(2).getInt(DatabaseConstants.FIELD_ID));
		assertEquals("id3", 2, data3.getInt("id"));
		assertEquals("name3", "dgonz", data3.getString("name"));
		assertEquals("age3", 12, data3.getInt("age"));
		assertEquals("gpa3", 3.7, data3.getDouble("gpa"));
		assertEquals("active3", true, data3.getBoolean("active"));
		assertEquals("ssn3", "111-55-3333", data3.getString("ssn"));
		
		data1 = new JSONObject();
		data1.put("id", 0);
		data1.put("name", "CARLOS");
		data1.put("age", 10);
		data1.put("ssn", "111-22-3333");
		data1.put("gpa", 3.5);
		data1.put("active", true);
		
		data2 = new JSONObject();
		data2.put("id", 1);
		data2.put("name", "DGONZ");
		data2.put("age", 11);
		data2.put("ssn", "111-44-3333");
		data2.put("gpa", 3.4);
		data2.put("active", true);
		
		data3 = new JSONObject();
		data3.put("id", 2);
		data3.put("name", "NANA");
		data3.put("age", 12);
		data3.put("ssn", "111-55-3333");
		data3.put("gpa", 3.3);
		data3.put("active", true);
		
		data4.put("id", 3);
		data4.put("name", "ANAN");
		data4.put("age", 1);
		data4.put("ssn", "123-66-3333");
		data4.put("gpa", 3.2);
		data4.put("active", false);
		
		newDocuments.add(data1);
		newDocuments.add(data2);
		newDocuments.add(data3);
		newDocuments.add(data4);
		
		
		replaceCriteriaArr.put("id");
		replaceCriteriaArr.put("ssn");
		changeOptions.put("addNew", true);
		changeOptions.put("markDirty", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(4, col.changeData(newDocuments, opt));
		
		results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		data4 = new JSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals(4, results.size());
		
		assertEquals("_id1", 1, results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("_id2", 2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("_id3", 3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("_id4", 4, results.get(3).get(DatabaseConstants.FIELD_ID));
		
		assertEquals("name1", "CARLOS", data1.get("name"));
		assertEquals("name2","DGONZ", data2.get("name"));
		assertEquals("name3", "NANA", data3.get("name"));
		assertEquals("name4", "ANAN", data4.get("name"));
		
		assertEquals("age1", 10, data1.get("age"));
		assertEquals("age2", 11, data2.get("age"));
		assertEquals("age3", 12, data3.get("age"));
		assertEquals("age4", 1, data4.get("age"));
		
		assertEquals("id1", 0, data1.get("id"));
		assertEquals("id2", 1, data2.get("id"));
		assertEquals("id3", 2, data3.get("id"));
		assertEquals("id4", 3, data4.get("id"));
		
		assertEquals("ssn1", "111-22-3333", data1.get("ssn"));
		assertEquals("ssn2", "111-44-3333", data2.get("ssn"));
		assertEquals("ssn3", "111-55-3333", data3.get("ssn"));
		assertEquals("ssn4", "123-66-3333", data4.get("ssn"));

		assertEquals("gpa1", 3.5, data1.get("gpa"));
		assertEquals("gpa2", 3.4, data2.get("gpa"));
		assertEquals("gpa3", 3.3, data3.get("gpa"));
		assertEquals("gpa4", 3.2, data4.get("gpa"));
	
		assertEquals("active1", true, data1.get("active"));
		assertEquals("active2", true, data2.get("active"));
		assertEquals("active3", true, data3.get("active"));
		assertEquals("active4", false, data4.get("active"));
	
	}	
	
	public void testGlorifiedAddChange() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
	}
	
	public void testAddNewFalseChange() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add" , 0, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
	
		assertEquals("docs", 0, results.size());
		
		
	}
	
	public void testImplictAddNewFalseChange() throws Exception {
		//add new is false default
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add" , 0, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
	
		assertEquals("docs", 0, results.size());
		

	}
	
	public void testMarkDirtyTrueChange() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", true);
		changeOptions.put("markDirty", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
		assertEquals("dirty docs", 3, col.countAllDirtyDocuments());
		

	}
	
	public void testMarkDirtyFalseChange() throws Exception{
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", true);
		changeOptions.put("markDirty", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
		assertEquals("dirty docs", 0, col.countAllDirtyDocuments());
		
	}
	
	public void testImplictMarkDirtyChange() throws Exception {
		//mark dirty is false default
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
		assertEquals("dirty docs", 0, col.countAllDirtyDocuments());
			
	}
	
	public void testDefaultReplaceCriteria() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		changeOptions.put("addNew", true);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
					
	}
	
	public void testChangeWithNoSF() throws Exception {
		//mark dirty is false default
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");

		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
				
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		
		
		changeOptions.put("addNew", true);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("faux add", 3, col.changeData(new JSONObject[]{data1,data2,data3}, opt));
		
		List<JSONObject> results = col.findAllDocuments();
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("docs", 3, results.size());
		
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("id3", 3, data3.get("id"));
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
	}
	
	public void testUsingEmptyArray() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("id", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject data4 = new JSONObject();
		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		data2.put("id", 2);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		data3.put("id", 3);
		data3.put("name", "mike");
		data3.put("age", 3);
		data4.put("id", 5);
		data4.put("name", "HEYOO");
		data4.put("age", 4);
		
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals(2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("id2", 2, data2.get("id"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("id3", 3, data3.get("id"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age"));
		
		
		
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(0, col.changeData(new JSONObject[]{}, opt));
		
	}
	
	public void testUsingMixedCaseSF() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("people");
		col.setSearchField("personID", SearchFieldType.INTEGER);
		col.setSearchField("firstName", SearchFieldType.STRING);
		col.setSearchField("lastName", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("personID", 1);
		data1.put("firstName", "nana");
		data1.put("lastName", "amfo");
		data1.put("age", 2);
		
		col.addData(data1);
		
		assertEquals("doc", 1, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(1,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("id", 1, data1.get("personID"));
		assertEquals("firstName", "nana", data1.get("firstName"));
		assertEquals("lastName", "amfo", data1.get("lastName"));
		assertEquals("age", 2, data1.get("age"));
		
		
		
		replaceCriteriaArr.put("personID");
		changeOptions.put("addNew", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		data2.put("personID", 1);
		data2.put("firstName", "carlos");
		data2.put("lastName", "anderu");
		data2.put("age", 4);
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(1, col.changeData(new JSONObject[]{data2}, opt));

			
	}
	
	public void testUsingMixedCaseASF() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("people");
		col.setSearchField("personID", SearchFieldType.INTEGER);
		col.setSearchField("firstName", SearchFieldType.STRING);
		col.setSearchField("lastName", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("firstID", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("personID", 1);
		data1.put("firstName", "nana");
		data1.put("lastName", "amfo");
		data1.put("age", 2);
		data1.put("firstID", 7);
		
		col.addData(data1);
		
		assertEquals("doc", 1, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(1,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("id", 1, data1.get("personID"));
		assertEquals("firstName", "nana", data1.get("firstName"));
		assertEquals("lastName", "amfo", data1.get("lastName"));
		assertEquals("age", 2, data1.get("age"));
		
		
		
		replaceCriteriaArr.put("firstID");
		changeOptions.put("addNew", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		data2.put("personID", 2);
		data2.put("firstName", "carlos");
		data2.put("lastName", "anderu");
		data2.put("age", 4);
		data2.put("firstID", 7);
		
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(1, col.changeData(new JSONObject[]{data2}, opt));

	}
	
	public void testChangeWithSameDocs() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("people");
		col.setSearchField("id", SearchFieldType.INTEGER);
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("id", 1);
		data1.put("name", "carlos");
		data1.put("age", 1);
		
		data2.put("id", 1);
		data2.put("name", "dgonz");
		data2.put("age", 2);
		
		data3.put("id", 2);
		data3.put("name", "mike");
		data3.put("age", 4);
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("doc", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("id2", 1, data2.get("id"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("id3", 2, data3.get("id"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 4, data3.get("age"));
		
		
		replaceCriteriaArr.put("id");
		changeOptions.put("addNew", false);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		
		data1 = new JSONObject();
		data1.put("id", 1);
		data1.put("name", "nana");
		data1.put("age", 3);
		
		data2 = new JSONObject();
		data2.put("id", 2);
		data2.put("name", "mike");
		data2.put("age", 4);
			
		
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals(3, col.changeData(new JSONObject[]{data1, data2}, opt));
		
		assertEquals("doc", 3, col.countAllDocuments());
		
		results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("id1", 1, data1.get("id"));
		assertEquals("name1", "nana", data1.get("name"));
		assertEquals("age1", 3, data1.get("age"));
		assertEquals("id2", 1, data2.get("id"));
		assertEquals("name2", "nana", data2.get("name"));
		assertEquals("age2", 3, data2.get("age"));
		assertEquals("id3", 2, data3.get("id"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 4, data3.get("age"));
		

	}
	

	

	
	
}
