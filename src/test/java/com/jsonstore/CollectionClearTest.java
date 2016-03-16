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
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionClearTest extends InstrumentationTestCase {
	
	public CollectionClearTest() {
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

	public void testClearSimple() throws JSONException, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		

		simpleCol.addData(new JSONObject("{age: 50, fn:'Mike'}"));
		simpleCol.addData(new JSONObject("{age: 50, fn:'Nana'}"));
		simpleCol.addData(new JSONObject("{age: 51, fn:'Carlos'}"));
		
		assertEquals("Expected 3 documents in the collection", 3, simpleCol.countAllDocuments());
		
		simpleCol.clearCollection();

		assertEquals("Expected no documents after a clear", 0, simpleCol.countAllDocuments());
		
		store.destroy();
	
	}
	
	public void testClearSimpleDatabaseNotOpen() throws JSONException, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Don't open. Try to cause an error
		
		try {
			simpleCol.clearCollection();
		} catch(JSONStoreDatabaseClosedException e) {
			store.destroy();
			return;
		}
		
		assertTrue("Expected a database closed exception to occur", false);		
		store.destroy();
	
	}
	
	public void testClearOpenWithoutDocuments() throws JSONException, JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		assertEquals("Expected no documents in the collection", 0, simpleCol.countAllDocuments());
		
		simpleCol.clearCollection();

		assertEquals("Expected no documents after a clear", 0, simpleCol.countAllDocuments());
		
		store.destroy();
	
	}
	
	
}
