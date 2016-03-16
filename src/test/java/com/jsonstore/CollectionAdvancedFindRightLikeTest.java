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

public class CollectionAdvancedFindRightLikeTest extends InstrumentationTestCase {

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
		JSONStoreAddOptions opt1 = new JSONStoreAddOptions();
		JSONStoreAddOptions opt2 = new JSONStoreAddOptions(); 
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("name", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("l337", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		opt1.addAdditionalSearchField("l337", "42");
		opt2.addAdditionalSearchField("l337", "44");
		
		simpleCol.addData(new JSONObject("{ name : 'carlosdgonz', age: 12}"), opt1);
		simpleCol.addData(new JSONObject("{ name : 'carlos', age: 22}"), opt2);
		simpleCol.addData(new JSONObject("{ name : 'Carlos', age: 32}"));
		simpleCol.addData(new JSONObject("{ name : 'carlosAnderu', age: 41}"), opt1);
		simpleCol.addData(new JSONObject("{ name : 'nana', age: 15 }"));
		
		return simpleCol;
	}
	
	private void databaseDestroy() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();
	}
	
	
	public void testAdvancedFindRightLikeStringProveFuzzy() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addRightLike("name", "carlos");
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(4, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlosdgonz"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("Carlos"));
		assertTrue(results.get(3).getJSONObject("json").getString("name").equals("carlosAnderu"));
	
		databaseDestroy();
	}
	
	public void testAdvancedFindRightLikeIntegerProveFuzzy() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addRightLike("age", 2);
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(1, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlos"));
	
		databaseDestroy();
	}
	
	public void testAdvancedFindRightLikeASF() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addRightLike("l337", "4");
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(3, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlosdgonz"));
		assertTrue(results.get(1).getJSONObject("json").getString("name").equals("carlos"));
		assertTrue(results.get(2).getJSONObject("json").getString("name").equals("carlosAnderu"));
		
		databaseDestroy();
	}
	
	public void testAdvancedFindRigthLikeMultiple() throws Throwable {
		JSONStoreCollection col = databaseSetup();
		
		JSONStoreQueryParts query = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		part.addRightLike("age", "4");
		part.addRightLike("name", "carlos");
		query.addQueryPart(part);
		List<JSONObject> results = col.findDocuments(query);
		
		assertEquals(1, results.size());
		assertTrue(results.get(0).getJSONObject("json").getString("name").equals("carlosAnderu"));
		
		
		databaseDestroy();
	}
	
}
