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

import static com.flowingcode.vaadin.jsonmigration.JsonMigrationHelper25.convertToJsonNode;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
class ElementalObjectNode extends ObjectNode implements UnsupportedJsonValueImpl {

  public ElementalObjectNode(JsonObject o) {
    super(JsonNodeFactory.instance, children(o));
  }

  private static Map<String, JsonNode> children(JsonObject o) {
    String keys[] = o.keys();
    switch (keys.length) {
      case 0:
        return Collections.emptyMap();
      case 1:
        return Collections.singletonMap(keys[0], convertToJsonNode(o.get(keys[0])));
      default:
        Map<String, JsonNode> children = new LinkedHashMap<>(keys.length);
        for (String key : keys) {
          children.put(key, convertToJsonNode(o.get(key)));
        }
        return children;
    }
  }

  @Override
  public JsonType getType() {
    return JsonType.OBJECT;
  }

}


