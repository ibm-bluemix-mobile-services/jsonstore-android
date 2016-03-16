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
import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.database.SearchFieldType;
import com.jsonstore.exceptions.JSONStoreException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CollectionRemoveDocumentTest extends InstrumentationTestCase {

	public CollectionRemoveDocumentTest() {
		// TODO Auto-generated constructor stub
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
	
	public void testRemoveDocumentSimple() throws JSONStoreException, JSONException {
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
		
		JSONStoreQueryParts qContent = new JSONStoreQueryParts();
		JSONStoreQueryPart qPart = new JSONStoreQueryPart();
		qPart.addEqual("fn", "Carlos");
		qContent.addQueryPart(qPart);
		List <JSONObject> carlosObj = simpleCol.findDocuments(qContent);	
		
		for(JSONObject carlos : carlosObj) {
			simpleCol.removeDocumentById(carlos.getInt(DatabaseConstants.FIELD_ID));
		}
		
		int countCarlos = simpleCol.countDocuments(qContent);
		int countAll = simpleCol.countAllDocuments();

		assertEquals("Count all should have returned 0 when looking again for Carlos docs", 0, countCarlos);
		assertEquals("Count all should have returned 2 when looking again for all docs", 2, countAll);
		
		
		store.destroy();
	}

}
