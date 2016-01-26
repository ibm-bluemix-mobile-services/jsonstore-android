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

package com.jsonstore.database;

public enum SearchFieldType {
	BOOLEAN("boolean", "INTEGER"), INTEGER("integer", "INTEGER"), NUMBER("number", "REAL"), STRING("string", "TEXT"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

	private String mappedType;
	private String name;

	private SearchFieldType(String name, String mappedType) {
		this.name = name;
		this.mappedType = mappedType;
	}

	public static SearchFieldType fromString(String s) {
		if (s.equals(BOOLEAN.name)) {
			return SearchFieldType.BOOLEAN;
		}

		else if (s.equals(INTEGER.name)) {
			return SearchFieldType.INTEGER;
		}

		else if (s.equals(NUMBER.name)) {
			return SearchFieldType.NUMBER;
		}

		else if (s.equals(STRING.name)) {
			return SearchFieldType.STRING;
		}

		return null;
	}

	public String getMappedType() {
		return this.mappedType;
	}

	public String getName() {
		return this.name;
	}
}
