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
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreDestroyFailureException;
import com.jsonstore.exceptions.JSONStoreException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionAdvancedFindNotBetweenTest extends InstrumentationTestCase {

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
	
	private JSONStoreCollection databaseSetup() throws JSONStoreException, JSONException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreAddOptions opt = new JSONStoreAddOptions();
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("last_name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.NUMBER);
		simpleCol.setSearchField("gpa", SearchFieldType.NUMBER);
		simpleCol.setSearchField("ssn", SearchFieldType.INTEGER);
		simpleCol.setSearchField("active", SearchFieldType.BOOLEAN);
		simpleCol.setAdditionalSearchField("l337", SearchFieldType.INTEGER);
	
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		opt.addAdditionalSearchField("l337", 42);
		
		simpleCol.addData(new JSONObject("{ name : 'mike', gpa: 1.0, ssn: 123, active: false, age: 24, last_name: 'ortman' }"), opt);
		simpleCol.addData(new JSONObject("{ name : 'carlos', gpa: 2.2, ssn: 345, active: false, age: 25, last_name: 'andreu' }"), opt);
		simpleCol.addData(new JSONObject("{ name : 'nana', gpa: 2.4, ssn: 567, active: true, age: 22, last_name: 'amfo' }"));
		simpleCol.addData(new JSONObject("{ name : 'dgonz', gpa: 3.6, ssn: 789, active: false, age: 25 }"));
		simpleCol.addData(new JSONObject("{ name : 'jeremy', gpa: 4.8, ssn: 987, active: true, age: 29 }"));
		simpleCol.addData(new JSONObject("{ name : 'famas' }"));
		simpleCol.addData(new JSONObject("{ name : 'andrew', last_name: 'ortman'}"));
		
		return simpleCol;
	}
	
	private void databaseDestroy() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
	}
	
	public void testAdvancedFindNotBetweenInteger() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotBetween("age", 25, 30);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals("docs", 2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("nana"));
		databaseDestroy();
	
	}
	
	public void testAdvancedFindNotBetweenIntegerNone() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotBetween("age", 22, 30);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(0, results.size());
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotBetweenIntegerMultiple() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotBetween("age", 25, 30);
		part.addNotBetween("gpa", 2.5, 3.4);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("nana"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindBetweenASF() throws Throwable {
		JSONStoreCollection col = databaseSetup();
	
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotBetween("l337", 20, 30);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		
	}
	
}
