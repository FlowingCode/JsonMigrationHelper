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

import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import org.objectweb.asm.Type;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

class ClassInstrumentationJacksonHelper {

  public static String getConvertedTypeDescriptor(Class<?> type) {
    if (type == JsonObject.class) {
      return Type.getDescriptor(ObjectNode.class);
    } else if (type == JsonArray.class) {
      return Type.getDescriptor(ArrayNode.class);
    } else if (type == JsonBoolean.class) {
      return Type.getDescriptor(BooleanNode.class);
    } else if (type == JsonNumber.class) {
      return Type.getDescriptor(DoubleNode.class);
    } else if (type == JsonString.class) {
      return Type.getDescriptor(StringNode.class);
    } else if (JsonValue.class.isAssignableFrom(type)) {
      return Type.getDescriptor(JsonNode.class);
    } else if (JsonValue[].class.isAssignableFrom(type)) {
      return "[" + getConvertedTypeDescriptor(type.getComponentType());
    }
    return Type.getDescriptor(type);
  }
}
