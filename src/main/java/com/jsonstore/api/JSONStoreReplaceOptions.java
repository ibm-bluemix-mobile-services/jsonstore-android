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
 * Options that are used to modify the replace operation in JSONStoreCollection.
 */
public class JSONStoreReplaceOptions {

	private boolean markDirty;

	public JSONStoreReplaceOptions() {
		markDirty = false;
	}

	/**
	 * Enable or disable the mark dirty flag.
	 * @param mark_dirty
	 *            If true, the document that is added will be marked dirty when replaced.
	 */
	public void setMarkDirty(boolean mark_dirty) {
		markDirty = mark_dirty;
	}

	/**
	 * Determine if the mark dirty flag is set or not.
	 * @return True if the setMarkDirty method has been called to mark the document
	 *            dirty when replaced.
	 * @see #setMarkDirty(boolean)
	 */
	public boolean isMarkDirty() {
		return markDirty;
	}

}
