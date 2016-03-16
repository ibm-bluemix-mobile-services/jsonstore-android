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
import com.jsonstore.api.JSONStoreFindOptions;
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreFindException;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionSortTest extends InstrumentationTestCase {

	
	public CollectionSortTest() {
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


	public void testSortWithMultipleProperties() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("firstname", SearchFieldType.STRING);
		col.setSearchField("lastname", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		JSONObject data4 = new JSONObject();
		
		data1.put("firstname", "carlos");
		data1.put("lastname", "anderu");
		data1.put("age", 16);
		data2.put("firstname", "mike");
		data2.put("lastname", "ortman");
		data2.put("age", 24);
		data3.put("firstname", "nana");
		data3.put("lastname", "amfo");
		data3.put("age", 24);
		data4.put("firstname", "daniel");
		data4.put("lastname", "gonzales");
		data4.put("age", 65);	
		
		col.addData(data2);
		col.addData(data1);
		col.addData(data3);
		col.addData(data4);
	
		JSONStoreFindOptions optFind = new JSONStoreFindOptions();
		optFind.sortBySearchFieldAscending("firstname");
		optFind.sortBySearchFieldAscending("lastname");
		optFind.sortBySearchFieldDescending("age");
	
		
		List<JSONObject> results = col.findAllDocuments(optFind);
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		data4 = new JSONObject(results.get(3).getString(DatabaseConstants.FIELD_JSON));
	
		assertEquals("docs", 4, col.countAllDocuments());
		assertEquals("firstname1", "carlos", data1.get("firstname"));
		assertEquals("lastname1", "anderu", data1.get("lastname"));
		assertEquals("age1", 16, data1.get("age"));
		assertEquals("firstname2", "daniel", data2.get("firstname"));
		assertEquals("lastname2", "gonzales", data2.get("lastname"));
		assertEquals("age2", 65, data2.get("age"));
		assertEquals("firstname3", "mike", data3.get("firstname"));
		assertEquals("lastname3", "ortman", data3.get("lastname"));
		assertEquals("age3", 24, data3.get("age"));
		assertEquals("firstname4", "nana", data4.get("firstname"));
		assertEquals("lastname4", "amfo", data4.get("lastname"));
		assertEquals("age4", 24, data4.get("age"));
		
		
	}
	
	public void testSortByInvalidSearchField() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreFindException err = null;
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
		
		JSONStoreFindOptions optFind = new JSONStoreFindOptions();
		optFind.sortBySearchFieldAscending("ssn");
	
		try
		{
			col.findAllDocuments(optFind);
		} catch(JSONStoreFindException e) {
			err = e;
		}
		
		assertNotNull(err);
		
	}

	public void testSortOffsetGreaterThanResults() throws Exception {
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
		data3.put("name", "carlos");
		data3.put("age", 3);
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("docs", 3, col.countAllDocuments());

		JSONStoreFindOptions optFind = new JSONStoreFindOptions();
		optFind.sortBySearchFieldAscending("name");
		optFind.sortBySearchFieldDescending("age");
		optFind.setLimit(1);
		optFind.setOffset(7);

		
			List<JSONObject> results = col.findAllDocuments(optFind);
			
			assertEquals("empty array", true, results.isEmpty());

	}

	public void testSortBoolean() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("name", SearchFieldType.STRING);
		col.setSearchField("issmart", SearchFieldType.BOOLEAN);

		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);

		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();

		data1.put("name", "carlos");
		data1.put("issmart", false);
		data2.put("name", "dgonz");
		data2.put("issmart", true);
		data3.put("name", "carlos");
		data3.put("issmart", true);
		
		col.addData(data1);
		col.addData(data2);
		col.addData(data3);
		
		assertEquals("docs", 3, col.countAllDocuments());
		
		JSONStoreFindOptions optFind = new JSONStoreFindOptions();
		optFind.sortBySearchFieldDescending("issmart");
		
		List<JSONObject> results = col.findAllDocuments(optFind);
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
		
		assertEquals("_id1", 2, results.get(0).get("_id"));
		assertEquals("name1", "dgonz", data1.get("name"));
		assertEquals("_id2", 3, results.get(1).get("_id"));
		assertEquals("name2", "carlos", data2.get("name"));
		assertEquals("_id3", 1, results.get(2).get("_id"));
		assertEquals("name3", "carlos", data3.get("name"));
		
	}
	

	
	
	
}
