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

public class CollectionAdvancedFindGreaterThanEqualTest extends InstrumentationTestCase {



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
	
		return simpleCol;
	}
	
	private void databaseDestroy() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
	}
	
	public void testAdvancedFindGreaterThanEqualsIntegerSpecific() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 24);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(4, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("jeremy"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindGreaterThanEqualsIntegerNone() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 30);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(0, results.size());
		
		databaseDestroy();
	}
	
	public void testAdvancedFindGreaterThanEqualsIntegerAll() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 22);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(5, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(4).getJSONObject("json").getString("name").equals("jeremy"));
		
		databaseDestroy();
	}
	
	
	public void testAdvancedFindGreaterThanEqualsFloatingPointSpecific() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 24.9);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(3, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("jeremy"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindGreaterThanEqualsMultiple() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 25);
		part.addGreaterThanOrEqual("ssn", 400);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("jeremy"));
		
	}
	
	public void testAdvancedFindGreaterThanEqualsASF() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("l337", 42);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
	
	}
	
	public void testAdvancedFindGreaterThanEqualsFloatingPointNone() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 29.1);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(0, results.size());
		
		databaseDestroy();
	}
	
	public void testAdvancedFindGreaterThanFloatingPointAll() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addGreaterThanOrEqual("age", 22);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(5, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(4).getJSONObject("json").getString("name").equals("jeremy"));
		
		databaseDestroy();
	}
}
