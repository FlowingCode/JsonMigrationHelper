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
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.function.SerializableConsumer;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.lang.reflect.Array;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
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

    int j = args.length - 1;
    Class<?> parameterTypes[] = method.getParameterTypes();
    if (args.length == parameterTypes.length) {
      if (method.isVarArgs() && args[j] instanceof Object[]) {
        Object[] convertedArray =
            convertArray((Object[]) args[j], parameterTypes[j].getComponentType());
        if (convertedArray != null) {
          convertedArgs = Arrays.copyOf(args, args.length);
          convertedArgs[j] = convertedArray;
        }
      }

      for (int i = 0; i < parameterTypes.length; i++) {
        if (args[i] instanceof JsonValue && parameterTypes[i] == BaseJsonNode.class) {

          if (convertedArgs == null) {
            convertedArgs = Arrays.copyOf(args, args.length);
          }
          convertedArgs[i] = convertToJsonNode((JsonValue) args[i]);
        }
      }
    }

    if (convertedArgs == null) {
      convertedArgs = args;
    }
    return method.invoke(instance, convertedArgs);
  }


  private static <T> T[] convertArray(Object[] array, Class<? extends T> newType) {
    T[] convertedArray = null;
    if (newType.isAssignableFrom(BaseJsonNode.class)) {
      for (int i = 0; i < array.length; i++) {
        if (array[i] instanceof JsonValue) {
          if (convertedArray == null) {
            @SuppressWarnings("unchecked")
            T[] copy = (newType == Object.class)
                ? (T[]) new Object[array.length]
                : (T[]) Array.newInstance(newType, array.length);
            if (i>0) {
              System.arraycopy(array, 0, copy, 0, i);
            }
            convertedArray = copy;
          }
          @SuppressWarnings("unchecked")
          T t = (T) convertToJsonNode((JsonValue) array[i]);
          convertedArray[i] = t;
        } else if (convertedArray != null) {
          convertedArray[i] = newType.cast(array[i]);
        }
      }
    }
    return convertedArray;
  }

  private static JsonValue convertToJsonValue(JsonNode jsonNode) {
    switch (jsonNode.getNodeType()) {
      case OBJECT:
        JsonObject jsonObject = Json.createObject();
        ObjectNode source = (ObjectNode) jsonNode;
        for (String key : source.propertyNames()) {
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
          arrayNode.add(convertToJsonNode(jsonArray.get(i)));
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


  @Override
  public ElementalPendingJavaScriptResult convertPendingJavaScriptResult(
      PendingJavaScriptResult result) {
    return new PendingJavaScriptResultImpl(result);
  }

  @SuppressWarnings("serial")
  @AllArgsConstructor
  private static final class PendingJavaScriptResultImpl
      implements ElementalPendingJavaScriptResult {
    private final PendingJavaScriptResult delegate;

    @SuppressWarnings("rawtypes")
    private static SerializableConsumer wrap(SerializableConsumer<JsonValue> resultHandler) {
      return (SerializableConsumer<JsonNode>) node -> resultHandler.accept(convertToJsonValue(node));
    };

    private static <T> T decodeAs(JsonNode node, Class<T> type) {
      return JsonCodec.decodeAs(convertToJsonValue(node), type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void then(SerializableConsumer<JsonValue> resultHandler,
        SerializableConsumer<String> errorHandler) {
      delegate.then(wrap(resultHandler), errorHandler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void then(Class<T> targetType, SerializableConsumer<T> resultHandler,
        SerializableConsumer<String> errorHandler) {
      if (targetType != null && JsonValue.class.isAssignableFrom(targetType)) {
        delegate.then(JsonNode.class, wrap(value->{
          resultHandler.accept(JsonCodec.decodeAs(value, targetType));
        }), errorHandler);
      } else {
        delegate.then(targetType, resultHandler, errorHandler);
      }
    }

    @Override
    public <T> CompletableFuture<T> toCompletableFuture(Class<T> targetType) {
      if (JsonValue.class.isAssignableFrom(targetType)) {
        return delegate.toCompletableFuture(JsonNode.class)
            .thenApply(node -> decodeAs(node, targetType));
      } else {
        return delegate.toCompletableFuture(targetType);
      }
    }

  }

}
