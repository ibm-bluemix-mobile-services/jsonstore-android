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
package com.jsonstore.exceptions;

/**
 * Thrown if the operating system has denied access to the file system, and causes
 * JSONStore to fail.
 */
public class JSONStoreFileAccessException extends JSONStoreException {

	private static final long serialVersionUID = 2257744189325144521L;

	public JSONStoreFileAccessException(String message) {
		super(message);
	}

	public JSONStoreFileAccessException(String message, Throwable t) {
		super(message, t);
	}

}
