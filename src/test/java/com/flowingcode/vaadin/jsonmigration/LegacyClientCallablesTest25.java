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

import com.vaadin.flow.component.Component;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

// this test doesn't work as-in because it requires Vaadin 25 in the classpath
public class LegacyClientCallablesTest25 extends LegacyClientCallablesTest {

  @Override
  protected <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    return new JsonMigrationHelper25().instrumentClass(clazz);
  }

  @Override
  protected Object createJsonNull() {
    return NullNode.getInstance();
  }

  @Override
  protected Object createJsonBoolean() {
    return BooleanNode.TRUE;
  }

  @Override
  protected Object createJsonNumber() {
    return DoubleNode.valueOf(42);
  }

  @Override
  protected Object createJsonString() {
    return StringNode.valueOf("test");
  }

  @Override
  protected Object createJsonArray() {
    return new ArrayNode(JsonNodeFactory.instance);
  }

  @Override
  protected ObjectNode createJsonObject() {
    return new ObjectNode(JsonNodeFactory.instance);
  }

  @Override
  protected Object createArrayOfJsonObject() {
    return new ObjectNode[] {createJsonObject(), createJsonObject()};
  }

}
