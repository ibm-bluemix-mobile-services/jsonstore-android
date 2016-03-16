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
import com.jsonstore.exceptions.JSONStoreAddException;
import com.jsonstore.exceptions.JSONStoreCloseAllException;
import com.jsonstore.exceptions.JSONStoreCountException;
import com.jsonstore.exceptions.JSONStoreDatabaseClosedException;
import com.jsonstore.exceptions.JSONStoreDestroyFailureException;
import com.jsonstore.exceptions.JSONStoreFileAccessException;
import com.jsonstore.exceptions.JSONStoreInvalidPasswordException;
import com.jsonstore.exceptions.JSONStoreInvalidSchemaException;
import com.jsonstore.exceptions.JSONStoreMigrationException;
import com.jsonstore.exceptions.JSONStoreSchemaMismatchException;
import com.jsonstore.exceptions.JSONStoreTransactionDuringInitException;
import com.jsonstore.exceptions.JSONStoreTransactionFailureException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionCountDocumentTest extends InstrumentationTestCase {

	public CollectionCountDocumentTest() {
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


	public void testCountCollectionSimple() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionFailureException, JSONStoreTransactionDuringInitException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		JSONObject obj = new JSONObject();
		obj.put("win", 123);		
		
		simpleCol.addData(obj);
		
		assertEquals("Count all should have returned just 1", simpleCol.countAllDocuments(), 1);

		store.destroy();
	}
	

	public void testCountCollectionQueryAdditionalSearchFields() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionDuringInitException, JSONStoreTransactionFailureException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("fn", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("state", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		JSONStoreAddOptions texasAdditionalOptions = new JSONStoreAddOptions();
		texasAdditionalOptions.addAdditionalSearchFields(new JSONObject("{state: 'TX'}"));
		
		JSONStoreAddOptions tennAdditionalOptions = new JSONStoreAddOptions();
		tennAdditionalOptions.addAdditionalSearchFields(new JSONObject("{state: 'TN'}"));
		
		//Add 7 documents
		simpleCol.addData(new JSONObject("{age: 50, fn:'Mike'}"), tennAdditionalOptions);
		simpleCol.addData(new JSONObject("{age: 50, fn:'Nana'}"), texasAdditionalOptions);
		simpleCol.addData(new JSONObject("{age: 51, fn:'Carlos'}"), texasAdditionalOptions);
		simpleCol.addData(new JSONObject("{age: 52, fn:'Dgonz'}"), texasAdditionalOptions);
		
		JSONStoreQueryParts queryTX = new JSONStoreQueryParts();
		JSONStoreQueryPart partTX = new JSONStoreQueryPart();
		partTX.addEqual("state", "TX");
		queryTX.addQueryPart(partTX);				
		int countTX = simpleCol.countDocuments(queryTX);
		
		JSONStoreQueryParts queryTN = new JSONStoreQueryParts();
		JSONStoreQueryPart partTN = new JSONStoreQueryPart();
		partTN.addEqual("state", "TN");
		queryTN.addQueryPart(partTN);				
		int countTN = simpleCol.countDocuments(queryTN);
		
		assertEquals("Count all should have returned 3 when looking for ASF TX (case sensitive!)", 3, countTX);
		assertEquals("Count all should have returned 1 when looking for ASF TN (case sensitive!)", 1, countTN);

		store.destroy();
	}
	
	public void testCountCollectionQuerySimpleFuzzy() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionFailureException, JSONStoreTransactionDuringInitException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("fn", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("state", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		simpleCol.addData(new JSONObject("{age: 52, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 53, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 54, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 55, fn:'dgonz'}"));
		
		JSONStoreQueryParts queryLower = new JSONStoreQueryParts();
		JSONStoreQueryPart partLower = new JSONStoreQueryPart();
		partLower.addLike("fn", "dgonz");
		queryLower.addQueryPart(partLower);				
		int countLower = simpleCol.countDocuments(queryLower);
		
		JSONStoreQueryParts queryUpper = new JSONStoreQueryParts();
		JSONStoreQueryPart partUpper = new JSONStoreQueryPart();
		partUpper.addLike("fn", "Dgonz");
		queryUpper.addQueryPart(partUpper);				
		int countUpper = simpleCol.countDocuments(queryUpper);
		
		assertEquals("Count all should have returned 4 when looking for dgonz (case insensitive!)", 4, countLower);
		assertEquals("Count all should have returned 4 when looking for Dgonz (case insensitive!)", 4, countUpper);

		store.destroy();
	}
	

	public void testCountCollectionQuerySimpleExact() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionFailureException, JSONStoreTransactionDuringInitException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("fn", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("state", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		simpleCol.addData(new JSONObject("{age: 52, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 53, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 54, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 55, fn:'dgonz'}"));
		
		JSONStoreQueryParts queryLower = new JSONStoreQueryParts();
		JSONStoreQueryPart partLower = new JSONStoreQueryPart();
		partLower.addEqual("fn", "dgonz");
		queryLower.addQueryPart(partLower);				
		int countLower = simpleCol.countDocuments(queryLower);
		
		JSONStoreQueryParts queryUpper = new JSONStoreQueryParts();
		JSONStoreQueryPart partUpper = new JSONStoreQueryPart();
		partUpper.addEqual("fn", "Dgonz");
		queryUpper.addQueryPart(partUpper);				
		int countUpper = simpleCol.countDocuments(queryUpper);
		
		assertEquals("Count all should have returned 1 when looking for dgonz (case sensitive!)", 1, countLower);
		assertEquals("Count all should have returned 3 when looking for Dgonz (case sensitive!)", 3, countUpper);

		store.destroy();
	}
	
	
	public void testCountCollectionQuerySimple() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionFailureException, JSONStoreTransactionDuringInitException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("fn", SearchFieldType.STRING);
		simpleCol.setSearchField("age", SearchFieldType.INTEGER);
		simpleCol.setAdditionalSearchField("state", SearchFieldType.STRING);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);

		//Add 7 documents
		simpleCol.addData(new JSONObject("{age: 50, fn:'Mike'}"));
		simpleCol.addData(new JSONObject("{age: 50, fn:'Nana'}"));
		simpleCol.addData(new JSONObject("{age: 51, fn:'Carlos'}"));
		simpleCol.addData(new JSONObject("{age: 52, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 53, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 54, fn:'Dgonz'}"));
		simpleCol.addData(new JSONObject("{age: 55, fn:'dgonz'}"));
		
		JSONStoreQueryParts query100 = new JSONStoreQueryParts();
		JSONStoreQueryPart part100 = new JSONStoreQueryPart();
		part100.addEqual("age", 100);
		query100.addQueryPart(part100);				
		int count100 = simpleCol.countDocuments(query100);
		
		JSONStoreQueryParts query51 = new JSONStoreQueryParts();
		JSONStoreQueryPart part51 = new JSONStoreQueryPart();
		part51.addEqual("age", 51);
		query51.addQueryPart(part51);				
		int count51 = simpleCol.countDocuments(query51);
		
		JSONStoreQueryParts query50 = new JSONStoreQueryParts();
		JSONStoreQueryPart part50 = new JSONStoreQueryPart();
		part50.addEqual("age", 50);
		query50.addQueryPart(part50);				
		int count50 = simpleCol.countDocuments(query50);
		
		
		assertEquals("Count all should have returned 0 when looking for age 100", 0, count100);
		assertEquals("Count all should have returned 1 when looking for age 51", 1, count51);
		assertEquals("Count all should have returned 2 when looking for age 50", 2, count50);

		store.destroy();
	}
	
	
	public void testCountCollectionMultiple() throws JSONStoreDestroyFailureException, JSONStoreInvalidSchemaException, JSONStoreFileAccessException, JSONStoreMigrationException, JSONStoreCloseAllException, JSONStoreInvalidPasswordException, JSONStoreSchemaMismatchException, JSONException, JSONStoreAddException, JSONStoreDatabaseClosedException, JSONStoreCountException, JSONStoreTransactionFailureException, JSONStoreTransactionDuringInitException {
		JSONStore store = JSONStore.getInstance(getTestContext());
		store.destroy();

		JSONStoreCollection simpleCol = new JSONStoreCollection("simple");
		simpleCol.setSearchField("win", SearchFieldType.INTEGER);
		
		//Open collection.
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		collections.add(simpleCol);
		store.openCollections(collections);
		
		int i = 0;
		for(i = 0; i < 100; i++) {
			JSONObject obj = new JSONObject();
			obj.put("win", i);			
			simpleCol.addData(obj);			
		}
		
		assertEquals("Count all should have returned 100", simpleCol.countAllDocuments(), 100);

		store.destroy();
		
	}
	
	
	
}
