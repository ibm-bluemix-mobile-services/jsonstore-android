# JSONStore Android

JSONStore is a lightweight, document-oriented storage system that enables persistent storage of JSON documents for Android applications.

# Features
* A simple API that allows developers to add, store, replace, search through documents without memorizing query syntax
* Ability to track local changes
	
# Dependencies
Add the following to your `build.gradle`
 
```Gradle 
android {
	packagingOptions {
		pickFirst 'META-INF/ASL2.0'
		pickFirst 'META-INF/LICENSE'
		pickFirst 'META-INF/NOTICE'
	}
}

compile 'org.codehaus.jackson:jackson-jaxrs:1.9.13'
compile 'com.google.guava:guava:14.0.1'
compile 'com.ibm.mobilefirstplatform.clientsdk.android:jsonstore:+'
```

> Note that the above statement will always import the most recent release of JSONStore. In case you're building a production application you might want to consider changing `+` to a specific version number.

# Usage

#### Initialize and open connections, get an Accessor, and add data

```Java
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
	ex.printStackTrace();
} catch (JSONException ex) {
	// Handle failure for any JSON parsing issues.
	ex.printStackTrace();		
}
```
	
#### Find - locate documents inside the Store

```Java
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
	ex.printStackTrace();
}
```

#### Replace - change the documents that are already stored inside a Collection

```Java
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
	ex.printStackTrace();	
}
```	
#### Remove - delete all documents that match the query

```Java
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
	ex.printStackTrace();
} catch (JSONException ex) {
	// Handle failure for any JSON parsing issues.
	ex.printStackTrace();
}
```	

#### Count - gets the total number of documents that match a query

```Java
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
	ex.printStackTrace();
}
```

#### Destroy - wipes data for all users, destroys the internal storage, and clears security artifacts

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
	// Destroy the Store.
	JSONStore.getInstance(ctx).destroy();
	
} catch (JSONStoreException ex) {
	// Handle failure for any of the previous JSONStore operations
	ex.printStackTrace();
}
```	

#### Security - close access to all opened Collections for the current user

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
    // Close access to all collections.
    JSONStore.getInstance(ctx).closeAll();
} 
catch (JSONStoreException ex) {
    // Handle failure for any of the previous JSONStore operations.
    ex.printStackTrace();
}
```

#### Security - enable encryption

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();
try {
	//Enable encryption
	JSONStore.getInstance(ctx).setEncryption(true);
} catch (Exception e){
	// Handle failure for any of the previous JSONStore operations.
	ex.printStackTrace();
}
```

> Note that enabling encryption requires installing additional components available from IBM MobileFirst Platform Foundation

#### Security - change the password that is used to access a Store

```Java
// The password should be user input. 
// It is hard-coded in the example for brevity.
String username = "carlos";
String oldPassword = "123";
String newPassword = "456";

// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
    JSONStore.getInstance(ctx).changePassword(oldPassword, newPassword, username);
} 
catch (JSONStoreException ex) {
    // Handle failure for any of the previous JSONStore operations.
    ex.printStackTrace();
} finally {
    // It is good practice to not leave passwords in memory
    oldPassword = null;
    newPassword = null;
}
```
#### Check whether a document is dirty

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
	// Get the already initialized collection.
	JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
	
	// Check if document with id '3' is dirty.
	boolean isDirty = peopleCollection.isDocumentDirty(3); 

} catch (JSONStoreException ex) {
 	// Handle failure for any of the previous JSONStore operations.
 	ex.printStackTrace();
}
```
#### Check the number of dirty documents

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
	// Get the already initialized collection.
	JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
	// Get the count of all dirty documents in the people collection.
	int totalDirty = peopleCollection.countAllDirtyDocuments();
} catch (JSONStoreException ex) {
	// Handle failure for any of the previous JSONStore operations.
	ex.printStackTrace();
}
```

#### Remove a Collection

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
	// Get the already initialized collection.
	JSONStoreCollection peopleCollection  = JSONStore.getInstance(ctx).getCollectionByName("people");
	
	// Remove the collection. The collection object is no longer usable.
	peopleCollection.removeCollection();
} catch (JSONStoreException ex) {
	// Handle failure for any of the previous JSONStore operations.
	ex.printStackTrace();
}
```

#### Clear all data that is inside a Collection

```Java
// Fill in the blank to get the Android application context.
Context ctx = getContext();

try {
	// Get the already initialized collection.	JSONStoreCollection peopleCollection = JSONStore.getInstance(ctx).getCollectionByName("people");
	
	// Clear the collection.
	peopleCollection.clearCollection();    
} catch (JSONStoreException ex) {
	// Handle failure for any of the previous JSONStore operations.
		ex.printStackTrace();
}
```

#### Start a transaction, add some data, remove a document, commit the transaction and roll back the transaction if there is a failure

```Java	
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
	ex.printStackTrace();
} catch (JSONException ex) {
	// Handle failure for any JSON parsing issues.
	// An exception occured. Take care of it to prevent further damage.
	
	JSONStore.getInstance(ctx).rollbackTransaction();
	ex.printStackTrace();
}
```

#### Get file information

```Java
Context ctx = getContext();
List<JSONStoreFileInfo> allFileInfo = JSONStore.getInstance(ctx).getFileInfo();

for(JSONStoreFileInfo fileInfo : allFileInfo) {
	long fileSize = fileInfo.getFileSizeBytes();
	String username = fileInfo.getUsername();
}
```	
	
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

## License

Copyright 2016 IBM Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
