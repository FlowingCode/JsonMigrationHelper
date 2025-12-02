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
import elemental.json.JsonType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;

@SuppressWarnings("serial")
class ElementalArrayNode extends ArrayNode implements UnsupportedJsonValueImpl {

  public ElementalArrayNode(JsonArray a) {
    super(JsonNodeFactory.instance, children(a));
  }

  private static List<JsonNode> children(JsonArray a) {
    switch (a.length()) {
      case 0:
        return Collections.emptyList();
      case 1:
        return Collections.singletonList(JsonMigrationHelper25.convertToJsonNode(a.get(0)));
      default:
        List<JsonNode> children = new ArrayList<>(a.length());
        for (int i = 0, n = a.length(); i < n; i++) {
          children.add(JsonMigrationHelper25.convertToJsonNode(a.get(i)));
        }
        return children;
    }
  }

  @Override
  public JsonType getType() {
    return JsonType.ARRAY;
  }

}

