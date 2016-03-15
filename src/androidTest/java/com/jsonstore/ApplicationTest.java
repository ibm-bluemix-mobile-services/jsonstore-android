package com.jsonstore;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.jsonstore.api.JSONStore;
import com.jsonstore.api.JSONStoreCollection;
import com.jsonstore.api.JSONStoreFindOptions;
import com.jsonstore.api.JSONStoreQueryPart;
import com.jsonstore.api.JSONStoreQueryParts;
import com.jsonstore.database.SearchFieldType;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
       super.setUp();
        createApplication();
    }

    public void testApp() throws Exception {

        List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
        JSONStore store = JSONStore.getInstance(getContext());
        store.destroy();

        // Create the collections object that will be initialized.
        JSONStoreCollection peopleCollection = new JSONStoreCollection("people");
        peopleCollection.setSearchField("name", SearchFieldType.STRING);
        peopleCollection.setSearchField("age", SearchFieldType.INTEGER);
        collections.add(peopleCollection);

        //Open Collection
        store.openCollections(collections);

        //Add Data to collection
        JSONObject newDocument = new JSONObject("{name: 'hayata', age: 1}");
        peopleCollection.addData(newDocument);

        //Find document
        JSONStoreQueryParts findQuery = new JSONStoreQueryParts();
        JSONStoreQueryPart part = new JSONStoreQueryPart();
        part.addLike("name", "hayata");
        part.addLessThan("age", 99);
        findQuery.addQueryPart(part);

        // Add additional find options (optional).
        JSONStoreFindOptions findOptions = new JSONStoreFindOptions();

        // Returns a maximum of 10 documents, default no limit.
        findOptions.setLimit(10);
        // Skip 0 documents, default no offset.
        findOptions.setOffset(0);

        // Search fields to return, default: ['_id', 'json'].
        findOptions.addSearchFilter("_id");
        findOptions.addSearchFilter("json");

        // How to sort the returned values, default no sort.
        findOptions.sortBySearchFieldAscending("name");
        findOptions.sortBySearchFieldDescending("age");

            // Find documents that match the query.
        List<JSONObject> results = peopleCollection.findDocuments(findQuery, findOptions);

        assertEquals(results.size(), 1);

        JSONObject retrievedDocument = results.get(0).getJSONObject("json");
        assertEquals(retrievedDocument.getString("name"), "hayata");
        assertEquals(retrievedDocument.getInt("age"), 1);

        store.changePassword("nana", "sdfdsf", "dsfs", 3);

    }

}



