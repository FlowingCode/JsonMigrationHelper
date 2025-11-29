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
    }
    return Type.getDescriptor(type);
  }

}
