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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JsonOrgJSONArrayDeserializer extends StdDeserializer<JSONArray> {
     protected static final JsonOrgJSONArrayDeserializer instance =
          new JsonOrgJSONArrayDeserializer();
     
     protected JsonOrgJSONArrayDeserializer () {
          super (JSONArray.class);
     }
     
     @Override
     public JSONArray deserialize (JsonParser parser,
          DeserializationContext context) throws IOException,
          JsonProcessingException {
          JSONArray result = new JacksonSerializedJSONArray();
          JsonToken token = parser.nextToken();
          
          while (token != JsonToken.END_ARRAY) {
               switch (token) {
                    case START_ARRAY: {
                         result.put (deserialize (parser, context));
                         
                         break;
                    }
                    
                    case START_OBJECT: {
                         result.put
                              (JsonOrgJSONObjectDeserializer.instance.deserialize
                                   (parser, context));
                         
                         break;
                    }
                    
                    case VALUE_EMBEDDED_OBJECT: {
                         result.put (parser.getEmbeddedObject());
                         
                         break;
                    }
                    
                    case VALUE_FALSE: {
                         result.put (Boolean.FALSE);
                         
                         break;
                    }
                    
                    case VALUE_NULL: {
                         result.put (JSONObject.NULL);
                         
                         break;
                    }
                    
                    case VALUE_NUMBER_FLOAT: {
                         result.put (parser.getNumberValue());
                         
                         break;
                    }
                    
                    case VALUE_NUMBER_INT: {
                         result.put (parser.getNumberValue());
                         
                         break;
                    }
                    
                    case VALUE_STRING: {
                         result.put (parser.getText());
                         
                         break;
                    }
                    
                    case VALUE_TRUE: {
                         result.put (Boolean.TRUE);
                         
                         break;
                    }
                    
                    case END_ARRAY: case END_OBJECT: case FIELD_NAME:
                         case NOT_AVAILABLE: {
                         break;
                    }
               }
               
               token = parser.nextToken();
          }
          
          return result;
     }
}
