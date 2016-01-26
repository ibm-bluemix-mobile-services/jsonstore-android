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

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JsonOrgJSONObjectDeserializer extends StdDeserializer<JSONObject> {
     protected static final JsonOrgJSONObjectDeserializer instance =
          new JsonOrgJSONObjectDeserializer();
     
     protected JsonOrgJSONObjectDeserializer () {
          super (JSONObject.class);
     }
     
     @Override
     public JSONObject deserialize (JsonParser parser,
          DeserializationContext context) throws IOException,
          JsonProcessingException {
          JSONObject result = new JacksonSerializedJSONObject();
          JsonToken token = parser.getCurrentToken();
          
          if (token == JsonToken.START_OBJECT) {
               token = parser.nextToken();
          }
          
          try {
               while (token != JsonToken.END_OBJECT) {
                    String name;

                    if (token != JsonToken.FIELD_NAME) {
                         throw context.wrongTokenException (parser,
                              JsonToken.FIELD_NAME, ""); //$NON-NLS-1$
                    }

                    name = parser.getCurrentName();
                    token = parser.nextToken();

                    switch (token) {
                         case START_ARRAY: {
                              result.put (name,
                                   JsonOrgJSONArrayDeserializer.instance.deserialize
                                        (parser, context));

                              break;
                         }

                         case START_OBJECT: {
                              result.put (name, deserialize (parser, context));

                              break;
                         }

                         case VALUE_EMBEDDED_OBJECT: {
                              result.put (name, parser.getEmbeddedObject());

                              break;
                         }

                         case VALUE_FALSE: {
                              result.put (name, Boolean.FALSE);

                              break;
                         }

                         case VALUE_NULL: {
                              result.put (name, JSONObject.NULL);

                              break;
                         }

                         case VALUE_NUMBER_FLOAT: {
                              result.put (name, parser.getNumberValue());

                              break;
                         }

                         case VALUE_NUMBER_INT: {
                              result.put (name, parser.getNumberValue());

                              break;
                         }

                         case VALUE_STRING: {
                              result.put (name, parser.getText());

                              break;
                         }

                         case VALUE_TRUE: {
                              result.put (name, Boolean.TRUE);

                              break;
                         }

                         case END_ARRAY: case END_OBJECT: case FIELD_NAME:
                         case NOT_AVAILABLE: {
                              break;
                         }
                    }
                    
                    token = parser.nextToken();
               }
          }
          
          catch (JSONException e) {
               throw context.mappingException (e.getMessage());
          }
          
          return result;
     }
}
