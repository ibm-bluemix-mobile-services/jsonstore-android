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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.SerializerBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;

public class JsonOrgJSONObjectSerializer extends SerializerBase<JSONObject>
{
     public final static JsonOrgJSONObjectSerializer instance =
          new JsonOrgJSONObjectSerializer();

     public JsonOrgJSONObjectSerializer () {
          super (JSONObject.class);
     }
     
     @Override
     public JsonNode getSchema (SerializerProvider provider, Type typeHint)
          throws JsonMappingException {
          return createSchemaNode ("object", true); //$NON-NLS-1$
     }
     
     @Override
     public void serialize(JSONObject value, JsonGenerator jgen,
          SerializerProvider provider) throws IOException,
          JsonGenerationException {
          jgen.writeStartObject();
          
          serializeContents (value, jgen, provider);
          
          jgen.writeEndObject();
     }
     
     protected void serializeContents(JSONObject value, JsonGenerator jgen,
          SerializerProvider provider) throws IOException,
          JsonGenerationException {
          Iterator<?> i  = value.keys();
          
          while (i.hasNext()) {
               String key = (String) i.next();
               Class<?> cls;
               Object obj;
               
               try {
                    obj = value.get (key);
               }
               
               catch (JSONException e) {
                    throw new JsonGenerationException (e);
               }
               
               if ((obj == null) || (obj == JSONObject.NULL)) {
                    jgen.writeNullField (key);
                    
                    continue;
               }
               
               jgen.writeFieldName (key);
               
               cls = obj.getClass();
               
               if ((cls == JSONObject.class) ||
                    JSONObject.class.isAssignableFrom (cls)) {
                    serialize ((JSONObject) obj, jgen, provider);
               }
               
               else if ((cls == JSONArray.class) ||
                    JSONArray.class.isAssignableFrom (cls)) {
                    JsonOrgJSONArraySerializer.instance.serialize
                         ((JSONArray) obj, jgen, provider);
               }
               
               else  if (cls == String.class) {
                    jgen.writeString ((String) obj);
               }
               
               else  if (cls == Integer.class) {
                    jgen.writeNumber (((Integer) obj).intValue());
               }
               
               else  if (cls == Long.class) {
                    jgen.writeNumber (((Long) obj).longValue());
               }
               
               else  if (cls == Boolean.class) {
                    jgen.writeBoolean (((Boolean) obj).booleanValue());
               }
               
               else  if (cls == Double.class) {
                    jgen.writeNumber(((Double) obj).doubleValue());
               }
               
               else {
                    provider.defaultSerializeValue (obj, jgen);
               }
          }
     }
     
     @Override
     public void serializeWithType (JSONObject value, JsonGenerator jgen,
          SerializerProvider provider, TypeSerializer typeSer)
          throws IOException, JsonGenerationException {
          typeSer.writeTypePrefixForObject (value, jgen);
          
          serializeContents (value, jgen, provider);
          
          typeSer.writeTypeSuffixForObject (value, jgen);
     }
}
