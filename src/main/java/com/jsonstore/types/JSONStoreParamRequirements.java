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

package com.jsonstore.types;

public class JSONStoreParamRequirements {

	private String name;
	private boolean required;
	private JSONStoreParameterType types[];

	//  Indicate if the Parameter values is loggable (true) or if it contain sensitive information,
	//  such as passwords, that should not be logged in the clear (false).  If this value is true, then 
	//  the BaseActionDispatcher will log the Parameter and value  when the subclass is dispatched.  If 
	//  this value is false, it is up to the subclass to log what is appropriate in its dispatch implementation.
	private boolean loggable;

	public JSONStoreParamRequirements(String name, boolean required, boolean loggable, JSONStoreParameterType types[]) {
		this.name = name;
		this.required = required;
		this.loggable = loggable;
		this.types = types;
	}

	public String getName() {
		return this.name;
	}

	public JSONStoreParameterType[] getTypes() {
		return this.types;
	}

	public boolean isRequired() {
		return this.required;
	}

	public boolean isLoggable() {
		return this.loggable;
	}
}
