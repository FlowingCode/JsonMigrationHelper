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

import elemental.json.JsonType;
import tools.jackson.databind.node.DoubleNode;

@SuppressWarnings("serial")
class ElementalNumberNode extends DoubleNode implements UnsupportedJsonValueImpl {

  public ElementalNumberNode(double value) {
    super(value);
  }

  @Override
  public String toJson() {
    double value = doubleValue();
    if (value == (long) value) {
      return String.valueOf((long) value);
    } else {
      return UnsupportedJsonValueImpl.super.toJson();
    }
  }

  @Override
  public JsonType getType() {
    return JsonType.NUMBER;
  }
}
