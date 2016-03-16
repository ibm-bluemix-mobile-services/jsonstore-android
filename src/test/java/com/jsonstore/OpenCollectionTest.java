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
import com.jsonstore.exceptions.JSONStoreDestroyFailureException;
import com.jsonstore.exceptions.JSONStoreException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.exceptions.JSONStoreSchemaMismatchException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;

import java.util.LinkedList;
import java.util.List;

public class OpenCollectionTest extends InstrumentationTestCase {

	public OpenCollectionTest() {
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



	public void testSimpleOpenCollection() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());

		assertEquals("Collection could not be located in JSONStore instance", store.getCollectionByName("simple"), simpleCol);

		store.destroy();

	}
	

	public void testMultipleOpenCollection() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);
		
		JSONStoreCollection anotherCol = new JSONStoreCollection("another");
		simpleCol.setSearchField("winrar", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		collections.add(anotherCol);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());
		
		assertEquals("Collection simple could not be located in JSONStore instance", store.getCollectionByName("simple"), simpleCol);
		assertEquals("Collection another could not be located in JSONStore instance", store.getCollectionByName("another"), anotherCol);

		store.destroy();

	}
	

	public void testMultipleOpenCollectionsWithNull() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		collections.add(null);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());
		
		assertEquals("Collection simple could not be located in JSONStore instance", store.getCollectionByName("simple"), simpleCol);
		
		store.destroy();

	}


	public void testReopenCollection() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());
		
		store.closeAll();

		store.openCollections(collections);

		assertTrue("Collection marked not reopened when reopened", simpleCol.wasReopened());

		store.destroy();

	}


	public void testNullSearchField() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField(null, SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertEquals("Search field list should be empty", simpleCol.getSearchFields().size(), 0);

		store.destroy();

	}


	public void testNullSearchFieldWithAdditionalNonNulls() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("realfield", SearchFieldType.BOOLEAN);
		simpleCol.setSearchField(null, SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertEquals("Search field list should only contain 1", simpleCol.getSearchFields().size(), 1);

		store.destroy();

	}


	public void testReopenCollectionMoreSearchFields() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());

		store.closeAll();

		simpleCol.setSearchField("fail", SearchFieldType.BOOLEAN);
		try {
			store.openCollections(collections);
		} catch (JSONStoreSchemaMismatchException e) {

			store.destroy();
			return;
		}

		store.destroy();
		assertTrue("Collection with different search fields did not throw a schema mismatch exception", false);

	}


	public void testReopenCollectionLessSearchFields() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);
		simpleCol.setSearchField("win2", SearchFieldType.BOOLEAN);
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertFalse("Collection marked reopened when not reopened", simpleCol.wasReopened());

		store.closeAll();

		simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);
		collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);

		try {
			store.openCollections(collections);
		} catch (JSONStoreSchemaMismatchException e) {

			store.destroy();
			return;
		}

		store.destroy();
		assertTrue("Collection with less search fields did not throw a schema mismatch exception", false);

	}


	public void testNullSearchFieldType() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("test", null);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertEquals("Search field list should be 1", simpleCol.getSearchFields().size(), 1);
		SearchFieldType t = simpleCol.getSearchFields().get("test");
		assertTrue("Default search field type should be string", t == SearchFieldType.STRING);

		store.destroy();

	}



	public void testNoSearchFields() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		assertEquals("Search field list should be 0", simpleCol.getSearchFields().size(), 0);

		store.destroy();

	}

	public void testCollectionNullName() throws JSONStoreDestroyFailureException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();
		try {
			new JSONStoreCollection(null);
		} catch (JSONStoreInvalidSchemaException e) {
			store.destroy();
			return;

		}

		assertTrue("Invalid schema should have been thrown!", false);
		store.destroy();

	}



	public void testNullCollection() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		//Open collection.
		List<JSONStoreCollection> collections = null;
		store.openCollections(collections);

		store.destroy();

	}



	public void testOpenNoCollections() throws JSONStoreException {
		JSONStore store = JSONStore.getInstance(getTestContext());

		store.destroy();

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		store.openCollections(collections);

		store.destroy();

	}
}
