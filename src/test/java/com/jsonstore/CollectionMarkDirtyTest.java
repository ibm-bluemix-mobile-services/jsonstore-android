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
import com.jsonstore.api.JSONStoreChangeOptions;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionMarkDirtyTest extends InstrumentationTestCase {

	
	public CollectionMarkDirtyTest() {
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

	public void testAddWithMarkDirtyTrue() throws Exception {
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
		
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.setMarkDirty(true);
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		col.addData(data3, addOptions);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDirtyDocuments();
		
		JSONObject obj = results.get(0);
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,obj.getInt(DatabaseConstants.FIELD_ID));
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals(2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age"));
		
	}
	
	public void testAddWithMarkDirtyFalse() throws Exception {
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
		
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.setMarkDirty(false);
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		col.addData(data3, addOptions);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDirtyDocuments();

		assertEquals(0, results.size());
		
		results = col.findAllDocuments();
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		
		assertEquals(3,results.size());
		assertEquals(1,results.get(0).get(DatabaseConstants.FIELD_ID));
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals(2, results.get(1).get(DatabaseConstants.FIELD_ID));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals(3, results.get(2).get(DatabaseConstants.FIELD_ID));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age3", 3, data3.get("age"));	
	}	
	
	public void testWithMixedSF() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("people");
		col.setSearchField("personID", SearchFieldType.STRING);
		col.setSearchField("firstName", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setSearchField("lastName", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("personID",1);
		data1.put("firstName", "nana");
		data1.put("lastName", "amfo");
		data1.put("age", 6);
		
		data2.put("personID", 1);
		data2.put("firstName", "dainel");
		data2.put("age", 2);
		data2.put("lastName", "gonzales");
		
		col.addData(data1);
		
		replaceCriteriaArr.put("personID");
		changeOptions.put("addNew", false);
		changeOptions.put("markDirty", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("change", 1, col.changeData(new JSONObject[]{data2}, opt));
		
		assertEquals("dirty", 1, col.countAllDirtyDocuments());
		
	}
	
	public void testWithMixedASF() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy(); 
		JSONObject changeOptions = new JSONObject();
		JSONArray replaceCriteriaArr = new JSONArray();
	
		JSONStoreCollection col = new JSONStoreCollection("people");
		col.setSearchField("personID", SearchFieldType.STRING);
		col.setSearchField("firstName", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setSearchField("lastName", SearchFieldType.STRING);
		col.setAdditionalSearchField("firstID", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("personID",5);
		data1.put("firstName", "nana");
		data1.put("lastName", "amfo");
		data1.put("age", 6);
		data1.put("firstID", 7);
		
		data2.put("personID", 4);
		data2.put("firstName", "dainel");
		data2.put("age", 2);
		data2.put("lastName", "gonzales");
		data2.put("firstID", 7);
		
		col.addData(data1);
		
		replaceCriteriaArr.put("firstID");
		changeOptions.put("addNew", false);
		changeOptions.put("markDirty", true);
		changeOptions.put("replaceCriteria", replaceCriteriaArr);
		JSONStoreChangeOptions opt =  new JSONStoreChangeOptions(changeOptions);
		
		assertEquals("change", 1, col.changeData(new JSONObject[]{data2}, opt));
		
		assertEquals("dirty", 1, col.countAllDirtyDocuments());
		

	}
		
}
