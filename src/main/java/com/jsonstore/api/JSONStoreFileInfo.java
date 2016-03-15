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

package com.jsonstore.api;

/**
 * Represents information about a JSONStore database file.
 */
public class JSONStoreFileInfo {

	private String username;
	private long fileSize;
	private boolean encrypted;
	
	JSONStoreFileInfo(String username, long file_size, boolean encrypted) {
		this.username = username;
		this.fileSize = file_size;
		this.encrypted = encrypted;
	}
	
	/**
	 * @return The name of the database file name.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @return The size of the database file in bytes.
	 */
	public long getFileSizeBytes() {
		return fileSize;
	}

	/**
	 * @return true if the file is encrypted, false otherwise.
	 */
	public boolean isEncrypted() {
		return encrypted;
	}

}
