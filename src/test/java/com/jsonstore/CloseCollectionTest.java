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
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreException;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CloseCollectionTest extends InstrumentationTestCase {

	public CloseCollectionTest() {
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



	public void testFindDocumentAfterClose() throws JSONStoreException, Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		try {
			JSONStoreQueryParts findContent = new JSONStoreQueryParts();
			JSONStoreQueryPart part = new JSONStoreQueryPart();
			part.addEqual("win", true);
			findContent.addQueryPart(part);
			simpleCol.findDocuments(findContent);
		} catch(JSONStoreDatabaseClosedException e)
		{
			err = e;
		}

		
		assertNotNull(err);

	}
	
	public void testAddDocumentAfterClose() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		JSONObject data3 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		data3.put("win", true);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		try {
			simpleCol.addData(data3);
		} catch(JSONStoreDatabaseClosedException e)
		{
			err = e;
		}

		
		assertNotNull(err);


	}
	
	public void testReplaceDocumentAfterClose() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		try {
			simpleCol.replaceDocument(new JSONObject("{\"_id\": 1, \"win\": false}"));
		} catch(JSONStoreDatabaseClosedException e)
		{
			err = e;
		}

		
		assertNotNull(err);

	}
	
	public void testCountDocumentsAfterClose() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		try {
			simpleCol.countAllDocuments();
		} catch(JSONStoreDatabaseClosedException e)
		{
			err = e;
		}

		
		assertNotNull(err);


	}
	
	public void testRemoveCollectionAfterClose() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		JSONStoreDatabaseClosedException err = null;
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		try {
			simpleCol.removeCollection();
		} catch(JSONStoreDatabaseClosedException e)
		{
			err = e;
		}
		
		assertNotNull(err);

	}
	
	public void testSimpleClose() throws Exception {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.BOOLEAN);

		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject data1 = new JSONObject();
		JSONObject data2 = new JSONObject();
		
		data1.put("win", true);
		data2.put("win",false);
		simpleCol.addData(data1);
		simpleCol.addData(data2);
		
		assertEquals("docs", 2, simpleCol.countAllDocuments());
		
		store.closeAll();
		
		store.destroy();
	}
	
	


	
}
