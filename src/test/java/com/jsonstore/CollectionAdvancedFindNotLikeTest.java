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

public class CollectionAdvancedFindNotLikeTest extends InstrumentationTestCase {

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
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.NUMBER);
		simpleCol.setSearchField("last_name", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		simpleCol.addData(new JSONObject("{ name : 'mike', age: 24, last_name: 'ortman' }"));
		simpleCol.addData(new JSONObject("{ name : 'carlos', age: 25, last_name: 'andreu' }"));
		simpleCol.addData(new JSONObject("{ name : 'nana', age: 22, last_name: 'amfo' }"));
		simpleCol.addData(new JSONObject("{ name : 'andrew', age: 24, last_name: 'ortman' }"));
		simpleCol.addData(new JSONObject("{ name : 'jeremy', age: 25 }"));
		simpleCol.addData(new JSONObject("{ name : 'famas' }"));
		
		return simpleCol;
	}
	
	private void databaseDestroy() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
	}
	
	
	public void testAdvancedFindNotLikeStringProveFuzzy() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotLike("last_name", "an");
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(1, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("nana"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotLikeIntegerProveFuzzyEmpty() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotLike("age", 2);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(0, results.size());
		
		databaseDestroy();
	}
	
	public void testAdvancedFindNotLikeIntegerProveFuzzySelect() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addNotLike("age", 5);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(3, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("mike"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("nana"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("andrew"));
		
		databaseDestroy();
	}
	
}
