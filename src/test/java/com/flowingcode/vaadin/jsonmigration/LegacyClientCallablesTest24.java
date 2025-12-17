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
import elemental.json.Json;
import elemental.json.JsonObject;

public class LegacyClientCallablesTest24 extends LegacyClientCallablesTest {

  @Override
  protected <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    return new LegacyJsonMigrationHelper().instrumentClass(clazz);
  }

  @Override
  protected Object createJsonNull() {
    return Json.createNull();
  }

  @Override
  protected Object createJsonBoolean() {
    return Json.create(true);
  }

  @Override
  protected Object createJsonNumber() {
    return Json.create(42);
  }

  @Override
  protected Object createJsonString() {
    return Json.create("test");
  }

  @Override
  protected Object createJsonArray() {
    return Json.createArray();
  }

  @Override
  protected JsonObject createJsonObject() {
    return Json.createObject();
  }

  @Override
  protected Object createArrayOfJsonObject() {
    return new JsonObject[] {createJsonObject(), createJsonObject()};
  }

}
