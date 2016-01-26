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

package com.jsonstore.jackson;

import org.json.JSONException;
import org.json.JSONObject;

public class JacksonSerializedJSONObject extends JSONObject {
     private JSONObject wrappedObject;
     
     public JacksonSerializedJSONObject () {
          super();
     }
     
     public JacksonSerializedJSONObject (JSONObject obj) {
          this.wrappedObject = obj;
     }
     
     @Override
     public String toString () {
          return JsonOrgModule.serialize ((this.wrappedObject == null) ? this :
               this.wrappedObject);
     }
     
     @Override
     public String toString (int indentFactor) throws JSONException {
          // We don't directly make use of this method, but if anyone does,
          // just use the default toString() method for performance.
          
          return toString();
     }
}
