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

public class CollectionCleanTest extends InstrumentationTestCase {

	
	public CollectionCleanTest() {
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



	public void testCleanAllDirtyDocs() throws Exception {
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
		JSONArray jArray = new JSONArray();
		jArray.put(data1);
		jArray.put(data2);
		jArray.put(data3);
		
		JSONStoreChangeOptions cOptions = new JSONStoreChangeOptions(new JSONObject("{\"markDirty\": true, \"addNew\": true}"));
		
		col.changeData(new JSONObject[]{data1}, cOptions);
		col.changeData(new JSONObject[]{data2}, cOptions);
		col.changeData(new JSONObject[]{data3}, cOptions);
		
		
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("docs", 3, results.size());
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
			
		results = col.findAllDirtyDocuments();
		
		assertEquals("dirty docs", 3, results.size());
		
		assertEquals("clean", 3, col.markDocumentsClean(results));
		
		assertEquals("none dirty", 0, col.countAllDirtyDocuments());
		
		
		
	}
	
	public void testCleanSomeDirtyDocs() throws Exception {
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
		
		
		JSONStoreChangeOptions cOptions = new JSONStoreChangeOptions(new JSONObject("{\"markDirty\": true, \"addNew\": true}"));
		assertEquals("changed", 3, col.changeData(new JSONObject[]{data1,data2,data3}, cOptions));
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("docs", 3, results.size());
		
		data1 = new JSONObject(results.get(0).getString(DatabaseConstants.FIELD_JSON));
		data2 = new JSONObject(results.get(1).getString(DatabaseConstants.FIELD_JSON));
		data3 = new JSONObject(results.get(2).getString(DatabaseConstants.FIELD_JSON));
	
		
		assertEquals("name1", "carlos", data1.get("name"));
		assertEquals("name2", "dgonz", data2.get("name"));
		assertEquals("name3", "mike", data3.get("name"));
		assertEquals("age1", 1, data1.get("age"));
		assertEquals("age2", 2, data2.get("age"));
		assertEquals("age3", 3, data3.get("age"));
		
		results = col.findAllDirtyDocuments();
		
		assertEquals("dirty docs", 3, results.size());
		
		assertEquals("clean partial", 2, col.markDocumentsClean(new JSONObject[] {results.get(0), results.get(1)}));
		
		assertEquals("one still dirty", 1, col.countAllDirtyDocuments());	
			
	}
	
}
