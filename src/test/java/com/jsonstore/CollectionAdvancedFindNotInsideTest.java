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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CollectionAdvancedFindNotInsideTest extends InstrumentationTestCase {

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
	
	public void testAdvancedFindNotInsideString() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> names = new ArrayList<Object>();
		names.add("carlos");
		names.add("mike");
		names.add("nana");
		
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("name", names);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals("docs", 4, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("jeremy"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("famas"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("andrew"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotInsideStringNone() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> names = new ArrayList<Object>();
		names.add("mike");
		names.add("nana");
		names.add("carlos");
		names.add("dgonz");
		names.add("jeremy");
		names.add("andrew");
		names.add("famas");
		
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("name", names);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals("docs", 0, results.size());
		databaseDestroy();
	}
	
	public void testAdvancedFindNotInsideStringMultiple() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> names = new ArrayList<Object>();
		List<Object> lastNames = new ArrayList<Object>();
		names.add("nana");
		names.add("mike");
		lastNames.add("ortman");
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("name", names);
		part.addNotInside("last_name", lastNames);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals("docs", 1, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlos"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotInsideInteger() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> ages = new ArrayList<Object>();
		ages.add(24);
		ages.add(25);
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("age", ages);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals("docs", 2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("jeremy"));
	
		databaseDestroy();
	
	}
	
	public void testAdvancedFindNotInsideIntegerNone() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> ages = new ArrayList<Object>();
		ages.add(22);
		ages.add(24);
		ages.add(25);
		ages.add(29);
		
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("age", ages);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(0, results.size());
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotInsideIntegerMultiple() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> ages = new ArrayList<Object>();
		List<Object> ssns = new ArrayList<Object>();
		ages.add(25);
		ages.add(22);
		ssns.add(987);
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotInside("age", ages);
		part.addNotInside("ssn", ssns);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(1, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindInsideBoolean() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> active = new ArrayList<Object>();
		active.add(true);
		active.add(false);
	
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addInside("active", active);
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
	
	public void testAdvancedFindInsideBooleanMixed() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> active = new ArrayList<Object>();
		active.add(1);
		active.add(0);
	
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addInside("active", active);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(5, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(4).getJSONObject("json").getString("name").equals("jeremy"));
		
		active = new ArrayList<Object>();
		active.add("1");
		active.add("0");
		
		query = new JSONStoreQueryParts();
		part = new JSONStoreQueryPart();
		part.addInside("active", active);
		query.addQueryPart(part);
		results = col.findDocuments(query);
		
		assertEquals(5, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("dgonz"));
		assertTrue(results.get(4).getJSONObject("json").getString("name").equals("jeremy"));
		
		databaseDestroy();	
	}
	
	
	public void testAdvancedFindInsideASF() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		List<Object> l337 = new ArrayList<Object>();
		l337.add(42);
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addInside("l337", l337);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(2, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		
	}
	
}
