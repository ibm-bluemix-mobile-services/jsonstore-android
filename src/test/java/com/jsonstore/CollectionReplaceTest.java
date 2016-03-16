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
import com.jsonstore.api.JSONStoreReplaceOptions;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreReplaceException;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionReplaceTest extends InstrumentationTestCase {

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

	
	public CollectionReplaceTest() {
		super();
	}

	public void testSimpleReplace() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("fn", SearchFieldType.STRING);
		col.setSearchField("ln", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("orderId", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("fn", "carlos");
		data1.put("ln", "anderu");
		data1.put("age", 13);
		data2.put("fn", "jeremy");
		data2.put("ln", "nortey");
		data2.put("age", 14);
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.addAdditionalSearchField("orderId", "abc123");
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		
		assertEquals("docs", 2, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("fn1", "carlos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln1", "anderu", results.get(0).getJSONObject("json").getString("ln"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 13, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		assertEquals("replace doc", 1, col.replaceDocument(new JSONObject("{\"_id\": 1, \"json\": {\"fn\" : \"carlitos\", \"age\": 99}}")));
		
		results = col.findAllDocuments();
		
		assertEquals("fn1", "carlitos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 99, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));	
		
	}
	
	public void testReplaceWithMarkDirty() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreReplaceOptions replaceOptions = new JSONStoreReplaceOptions();
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("fn", SearchFieldType.STRING);
		col.setSearchField("ln", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("orderId", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("fn", "carlos");
		data1.put("ln", "anderu");
		data1.put("age", 13);
		data2.put("fn", "jeremy");
		data2.put("ln", "nortey");
		data2.put("age", 14);
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.addAdditionalSearchField("orderId", "abc123");
		addOptions.setMarkDirty(false);
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		
		assertEquals("docs", 2, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("fn1", "carlos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln1", "anderu", results.get(0).getJSONObject("json").getString("ln"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 13, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		assertEquals("dirty docs", 0, col.countAllDirtyDocuments());
		
		
		replaceOptions.setMarkDirty(true);
		assertEquals("replace doc", 1, col.replaceDocument(new JSONObject("{\"_id\": 1, \"json\": {\"fn\" : \"carlitos\", \"age\": 99}}"), replaceOptions));
		
		results = col.findAllDocuments();
		
		assertEquals("fn1", "carlitos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 99, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		assertEquals("dirty docs", 1, col.countAllDirtyDocuments());
		
		results = col.findAllDirtyDocuments();
		
		assertEquals("dirty doc name", "carlitos", results.get(0).getJSONObject("json").getString("fn"));
	}
	
	public void testReplaceNull() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("fn", SearchFieldType.STRING);
		col.setSearchField("ln", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("orderId", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("fn", "carlos");
		data1.put("ln", "anderu");
		data1.put("age", 13);
		data2.put("fn", "jeremy");
		data2.put("ln", "nortey");
		data2.put("age", 14);
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.addAdditionalSearchField("orderId", "abc123");
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		
		assertEquals("docs", 2, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("fn1", "carlos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln1", "anderu", results.get(0).getJSONObject("json").getString("ln"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 13, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		
		assertEquals("no replace", 0, col.replaceDocument(null));

		results = col.findAllDocuments();
				
		assertEquals("fn1", "carlos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln1", "anderu", results.get(0).getJSONObject("json").getString("ln"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 13, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		
	}
	
	public void testReplaceInvalidID() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
		JSONStoreReplaceException err = null;
		JSONStoreCollection col = new JSONStoreCollection("customers");
		col.setSearchField("fn", SearchFieldType.STRING);
		col.setSearchField("ln", SearchFieldType.STRING);
		col.setSearchField("age", SearchFieldType.INTEGER);
		col.setAdditionalSearchField("orderId", SearchFieldType.STRING);
		
		//Open collection
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(col);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("fn", "carlos");
		data1.put("ln", "anderu");
		data1.put("age", 13);
		data2.put("fn", "jeremy");
		data2.put("ln", "nortey");
		data2.put("age", 14);
		
		JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
		addOptions.addAdditionalSearchField("orderId", "abc123");
		col.addData(data1, addOptions);
		col.addData(data2, addOptions);
		
		assertEquals("docs", 2, col.countAllDocuments());
		
		List<JSONObject> results = col.findAllDocuments();
		
		assertEquals("fn1", "carlos", results.get(0).getJSONObject("json").getString("fn"));
		assertEquals("fn2",  "jeremy", results.get(1).getJSONObject("json").getString("fn"));
		assertEquals("ln1", "anderu", results.get(0).getJSONObject("json").getString("ln"));
		assertEquals("ln2", "nortey", results.get(1).getJSONObject("json").getString("ln"));
		assertEquals("age1", 13, results.get(0).getJSONObject("json").getInt("age"));
		assertEquals("age2", 14, results.get(1).getJSONObject("json").getInt("age"));
		
		
		try {
			col.replaceDocument(new JSONObject("{\"_id\": 99, \"json\": {\"fn\" : \"carlitos\", \"age\": 99}}"));
		} catch(JSONStoreReplaceException e) {
			err = e;
		}
		
		assertNotNull(err);
		
		
	}
	
	
	
	
	
}
