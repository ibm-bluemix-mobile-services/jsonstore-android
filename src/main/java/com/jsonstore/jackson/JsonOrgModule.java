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

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;

public class JsonOrgModule extends SimpleModule {
     private static final ObjectMapper mapper = new ObjectMapper();
     
     static {
          // Register the Jackson -> json.org serializers and deserializers.
          
          JsonOrgModule.mapper.registerModule (new JsonOrgModule());
     }
     
     private JsonOrgModule () {
          super ("JsonOrgModule", new Version (1, 0, 0, null)); //$NON-NLS-1$
          
          addDeserializer (JSONArray.class,
               JsonOrgJSONArrayDeserializer.instance);
          addDeserializer (JSONObject.class,
               JsonOrgJSONObjectDeserializer.instance);
          
          addSerializer (JSONArray.class, JsonOrgJSONArraySerializer.instance);
          addSerializer (JSONObject.class,
               JsonOrgJSONObjectSerializer.instance);
     }
     
     public static JSONArray deserializeJSONArray (String json)
          throws Throwable {
          return JsonOrgModule.mapper.readValue (json, JSONArray.class);
     }
     
     public static JSONObject deserializeJSONObject (String json)
          throws Throwable {
          return JsonOrgModule.mapper.readValue (json, JSONObject.class);
     }
     
     public static String serialize (JSONArray array) {
          try {
               StringWriter writer = new StringWriter();
               
               JsonOrgModule.mapper.writeValue (writer, array);
               
               writer.close();
               
               return writer.toString();
          }
          
          catch (Throwable e) {
               return null;
          }
     }
     
     public static String serialize (JSONObject obj) {
          try {
               StringWriter writer = new StringWriter();
               
               JsonOrgModule.mapper.writeValue (writer, obj);
               
               writer.close();
               
               return writer.toString();
          }
          
          catch (Throwable e) {
               return null;
          }
     }
}
