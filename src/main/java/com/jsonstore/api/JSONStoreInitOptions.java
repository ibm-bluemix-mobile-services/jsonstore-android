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

import com.jsonstore.database.DatabaseConstants;
import com.jsonstore.security.SecurityUtils;

/**
 * Options that are used to modify the init operation in JSONStore.
 */
public final class JSONStoreInitOptions {
	private String username, password, secureRandom;
	private boolean clear = false;
	private int pbkdf2Iterations = SecurityUtils.PBKDF2_ITERATIONS; //The default has to remain 10000, or all applications from previous versions will crash.

	public JSONStoreInitOptions(String username) {
		setUsername(username);
		setPassword("");
		setSecureRandom(null);
	}
	
	public JSONStoreInitOptions() {
		setUsername(null);
		setPassword("");
		setSecureRandom(null);
	}

	/**
	 * Get the user name.
	 * @return String The user name.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the user name to be used for the JSONStore.
	 * @param new_username the user name to be used.
	 */
	public void setUsername(String new_username) {
		this.username =  (new_username == null || new_username.isEmpty()) ? DatabaseConstants.DEFAULT_USERNAME : new_username;
		
	}

	/**
	 * @exclude
	 * @return true if is clear
	 */
	public boolean isClear() {
		return clear;
	}
	
	/**
	 * @exclude
	 * @param clear
	 */
	public void setClear(boolean clear) {
		this.clear = clear;
	}

	/**
	 * Set the password to be used for the store.
	 * @param new_password String The password to be used for the store.
	 */
	public void setPassword(String new_password) {
		this.password = new_password == null ? "" : new_password; //$NON-NLS-1$
	}

	/**
	 * Get the password to be used for the store.
	 * @return password String The password to be used for the store.
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * Get the secure random that is used by security artifacts.
	 * @return The secure random string to be used by security artifacts.
	 */
	public String getSecureRandom() {
		return secureRandom;
	}

	/**
	 * Set the secure random to be used by security artifacts.
	 * @param secureRandom The secure random string to be used by security artifacts.
	 */
	public void setSecureRandom(String secureRandom) {
		this.secureRandom = secureRandom;
	}


	public int getPBKDF2Iterations() {
		return pbkdf2Iterations;
	}

	public void setPBKDF2Iterations(int pbkdf2Iterations) {
		this.pbkdf2Iterations = pbkdf2Iterations;
	}




}
