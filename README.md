# JSONStore Android

JSONStore is a lightweight, document-oriented storage system that enables persistent storage of JSON documents for Android applications.

# Features
* A simple API that gives developers to add, store, replace, search through documents without memorizing query syntax
* Ability to track local changes
	
**Note on Security**: By default security has been disabled so please review this blog post to enable its usage in JSONStore. 
# Usage

#### Initialize and open connections, get an Accessor, and add data
	// Fill in the blank to get the Android application context.
	Context ctx = getContext();

	try {
		List<JSONStoreCollection> collections = new LinkedList<JSONStoreCollection>();
		
		// Create the collections object that will be initialized.
		JSONStoreCollection peopleCollection = new JSONStoreCollection("people");
		peopleCollection.setSearchField("name", SearchFieldType.STRING);
		peopleCollection.setSearchField("age", SearchFieldType.INTEGER);
		collections.add(peopleCollection);
	
	  // Open the collection.

	  JSONStore.getInstance(ctx).openCollections(collections, initOptions);

	  // Add data to the collection.
	  JSONObject newDocument = new JSONObject("{name: 'saitama', age: 10}");
	  JSONStoreAddOptions addOptions = new JSONStoreAddOptions();
	  addOptions.setMarkDirty(true);
	  peopleCollection.addData(newDocument, addOptions);
	  
	} catch (JSONStoreException ex) {
	// Handle failure for any of the previous JSONStore operations (init, add).
		throw ex;
	} catch (JSONException ex) {
	// Handle failure for any JSON parsing issues.
		throw ex;
	}
	
#### Find - locate documents inside the Store

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();

	try {
		// Get the already initialized collection.
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		
		JSONStoreQueryParts findQuery = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		
		part.addLike("name", "genos");
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
		findOptions.sortBySeachFieldDescending("age");
		
		// Find documents that match the query.
		List<JSONObject> results = peopleCollection.findDocuments(findQuery, findOptions);
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations
		throw ex;
	}

#### Replace - change the documents that are already stored inside a Collection

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
		// Get the already initialized collection.
	
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
	
	 	// Documents will be located with their '_id' field  and replaced with the data in the 'json' field.
	
		JSONObject replaceDoc = new JSONObject("{_id: 1, json: {name: 'kenshin', age: 99}}");
	
		// Mark data as dirty (true = yes, false = no), default true.
		JSONStoreReplaceOptions replaceOptions = new JSONStoreReplaceOptions();
		replaceOptions.setMarkDirty(true);
	
		// Replace the document.
		peopleCollection.replaceDocument(replaceDoc, replaceOptions);
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations.
		throw ex;	
	}
	
#### Remove - delete all documents that match the query

	// Fill in the blank to get the Android application context.
	
	Context ctx = getContext();
	
	try {
	
		// Get the already initialized collection.
	
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
	
		// Documents will be located with their '_id' field.
		int id = 1;
	
		JSONStoreRemoveOptions removeOptions = new JSONStoreRemoveOptions();
	
		// Mark data as dirty (true = yes, false = no), default true.
		removeOptions.setMarkDirty(true);
	
		// Replace the document.
		peopleCollection.removeDocumentById(id, removeOptions);
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations
		throw ex;
	} catch (JSONException ex) {
		// Handle failure for any JSON parsing issues.
		throw ex;
	}
	
#### Count - gets the total number of documents that match a query

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	try {
	
		// Get the already initialized collection.
		
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		
		// Count all documents that match the query.
		JSONStoreQueryParts countQuery = new JSONStoreQueryParts();
		JSONStoreQueryPart part = new JSONStoreQueryPart();
		
		// Exact match.
		part.addEqual("name", "shu");
		countQuery.addQueryPart(part);
		
		// Replace the document.
		int resultCount = peopleCollection.countDocuments(countQuery);
		JSONObject doc = peopleCollection.findDocumentById(resultCount);
		peopleCollection.replaceDocument(doc);
		
	} catch (JSONStoreException ex) {
		throw ex;
	}

#### Destroy - wipes data for all users, destroys the internal storage, and clears security artifacts

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
		// Destroy the Store.
		JSONStore.getInstance(ctx).destroy();
		
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations
		throw ex;
	}
	
#### Security - close access to all opened Collections for the current user	
    // Fill in the blank to get the Android application context.
     Context ctx = getContext();

    try {
        // Close access to all collections.
        WLJSONStore.getInstance(ctx).closeAll();
    } 
    catch (JSONStoreException ex) {
        // Handle failure for any of the previous JSONStore operations.
        throw ex;
    }

#### Security - change the password that is used to access a Store	
    // The password should be user input. 
    // It is hard-coded in the example for brevity.
    String username = "carlos";
    String oldPassword = "123";
    String newPassword = "456";

    // Fill in the blank to get the Android application context.
    Context ctx = getContext();

    try {
        WLJSONStore.getInstance(ctx).changePassword(oldPassword, newPassword, username);
    } 
    catch (JSONStoreException ex) {
        // Handle failure for any of the previous JSONStore operations.
        throw ex;
    } finally {
        // It is good practice to not leave passwords in memory
        oldPassword = null;
        newPassword = null;
    }
#### Check whether a document is dirty

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();

	try {
		// Get the already initialized collection.
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		
		// Check if document with id '3' is dirty.
		boolean isDirty = peopleCollection.isDocumentDirty(3); 
	
	} catch (JSONStoreException ex) {
	 	// Handle failure for any of the previous JSONStore operations.
	 	throw ex;
	}

#### Check the number of dirty documents
	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
			// Get the already initialized collection.
			JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		
			// Get the count of all dirty documents in the people collection.
			int totalDirty = peopleCollection.countAllDirtyDocuments();
		} catch (JSONStoreException ex) {
			// Handle failure for any of the previous JSONStore operations.
			throw ex;
		}

#### Remove a Collection

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
		// Get the already initialized collection.
		JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
		
		// Remove the collection. The collection object is no longer usable.
		peopleCollection.removeCollection();
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations.
		throw ex;
	}

#### Clear all data that is inside a Collection

	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
	
		// Get the already initialized collection.	
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		
		// Clear the collection.
		peopleCollection.clearCollection();    
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations.
			throw ex;
	}
	
#### Start a transaction, add some data, remove a document, commit the transaction and roll back the transaction if there is a failure
	
	// Fill in the blank to get the Android application context.
	Context ctx = getContext();
	
	try {
		// Get the already initialized collection.
		JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
		JSONStore.getInstance(ctx).startTransaction();
		
		JSONObject docToAdd = new JSONObject("{name: 'aiko', age: 99}");
		
		// Find documents that match query.
		peopleCollection.addData(docToAdd);
		
		//Remove added doc.
		int id = 1;
		peopleCollection.removeDocumentById(id);
		JSONStore.getInstance(ctx).commitTransaction();
	} catch (JSONStoreException ex) {
		// Handle failure for any of the previous JSONStore operations.
		// An exception occured. Take care of it to prevent further damage.
		JSONStore.getInstance(ctx).rollbackTransaction();
			throw ex;
	} catch (JSONException ex) {
		// Handle failure for any JSON parsing issues.
		// An exception occured. Take care of it to prevent further damage.
		
			JSONStore.getInstance(ctx).rollbackTransaction();
			throw ex;
	}

#### Get file information
	Context ctx = getContext();
	List<JSONStoreFileInfo> allFileInfo = JSONStore.getInstance(ctx).getFileInfo();

	for(JSONStoreFileInfo fileInfo : allFileInfo) {
		long fileSize = fileInfo.getFileSizeBytes();
		String username = fileInfo.getUsername();
	}
	
	
# Logger

JSONStore uses logback-android which is a SL4J facade on top of Google's logging platform. This provides users more customization than the normal Android logger capabilities. To configure the logger you will need to create `assets/logback.xml`. For more information please review the logback-android [documentation](https://github.com/tony19/logback-android).

    ```XML
    	<configuration>
    		<!-- Create a logcat appender -->
		    <!-- Create a file appender for a log in the application's data directory -->
			    <appender name="log" class="ch.qos.logback.classic.android.LogcatAppender">
        			<encoder>
		            	<pattern>%-5level %logger{36} - %msg</pattern>
       				</encoder>
    			</appender>

	    <!-- Write TRACE (and higher-level) messages to the log file -->
    		<root level="TRACE">
        		<appender-ref ref="log" />
    		</root>
		</configuration>
	```

# License

This project is licensed under the terms of the Apache 2 license.
> You can find the license [here](https://github.com/ibm-bluemix-mobile-services/jsonstore-android/blob/development/LICENSE).