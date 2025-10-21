/*-
 * #%L
 * Json Migration Helper
 * %%
 * Copyright (C) 2025 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.jsonmigration;

import java.lang.reflect.Method;
import java.util.Arrays;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BaseJsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@NoArgsConstructor
class JsonMigrationHelper25 implements JsonMigrationHelper {

  @Override
  public JsonValue convertToJsonValue(Object object) {
    if (object instanceof JsonValue) {
      return (JsonValue) object;
    } else if (object instanceof JsonNode) {
      return convertToJsonValue((JsonNode) object);
    } else if (object == null) {
      return null;
    } else {
      throw new ClassCastException(
          object.getClass().getName() + " cannot be converted to elemental.json.JsonObject");
    }
  } 

  @Override
  @SneakyThrows
  public Object invoke(Method method, Object instance, Object... args) {
    Object[] convertedArgs = null;
    Class<?> parameterTypes[] = method.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      if (args[i] instanceof JsonValue && parameterTypes[i] == BaseJsonNode.class) {
        
        if (convertedArgs == null) {
          convertedArgs = Arrays.copyOf(args, args.length);
        }
        convertedArgs[i] = convertToJsonNode((JsonValue) args[i]);
      }
    }
    if (convertedArgs == null) {
      convertedArgs = args;
    }
    return method.invoke(instance, convertedArgs);
  }

  private static JsonValue convertToJsonValue(JsonNode jsonNode) {
    switch (jsonNode.getNodeType()) {
      case OBJECT:
        JsonObject jsonObject = Json.createObject();
        JsonObject source = (JsonObject)jsonNode; 
        for (String key : source.keys()) {
          jsonObject.put(key, convertToJsonValue(source.get(key)));
        }
        return jsonObject;
      case ARRAY:
        JsonArray jsonArray = Json.createArray();
        for (int i = 0; i < jsonNode.size(); i++) {
          jsonArray.set(i, convertToJsonValue(jsonNode.get(i)));
        }
        return jsonArray;
      case STRING:
        return Json.create(jsonNode.asText());
      case NUMBER:
        return Json.create(jsonNode.asDouble());
      case BOOLEAN:
        return Json.create(jsonNode.asBoolean());
      case NULL:
        return Json.createNull();
      default:
        throw new IllegalArgumentException("Unsupported JsonNode type: " + jsonNode.getNodeType());
    }
  }

  private static final JsonNodeFactory nodeFactory = JsonNodeFactory.instance;

  private static BaseJsonNode convertToJsonNode(JsonValue jsonValue) {
    switch (jsonValue.getType()) {
      case OBJECT:
        JsonObject jsonObject = (JsonObject) jsonValue;
        ObjectNode objectNode = nodeFactory.objectNode();
        for (String key : jsonObject.keys()) {
          objectNode.set(key, convertToJsonNode(jsonObject.get(key)));
        }
        return objectNode;

      case ARRAY:
        JsonArray jsonArray = (JsonArray) jsonValue;
        ArrayNode arrayNode = nodeFactory.arrayNode(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
          arrayNode.set(i, convertToJsonNode(jsonArray.get(i)));
        }
        return arrayNode;

      case STRING:
        return nodeFactory.textNode(jsonValue.asString());

      case NUMBER:
        return nodeFactory.numberNode(jsonValue.asNumber());

      case BOOLEAN:
        return nodeFactory.booleanNode(jsonValue.asBoolean());

      case NULL:
        return nodeFactory.nullNode();

      default:
        throw new IllegalArgumentException("Unsupported JsonValue type: " + jsonValue.getType());
    }
  }

}
