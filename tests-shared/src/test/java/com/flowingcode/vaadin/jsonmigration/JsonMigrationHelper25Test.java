/*-
 * #%L
 * Json Migration Helper
 * %%
 * Copyright (C) 2025 - 2026 Flowing Code
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

import static org.junit.Assert.assertTrue;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNull;
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;
import org.junit.Test;

public class JsonMigrationHelper25Test {

  private final JsonMigrationHelper25 helper = new JsonMigrationHelper25();

  @Test
  public void testConvertToClientCallableResult_JsonObject() {
    JsonObject input = Json.createObject();
    input.put("key", "value");

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonObject", result instanceof JsonObject);
  }

  @Test
  public void testConvertToClientCallableResult_JsonArray() {
    JsonArray input = Json.createArray();
    input.set(0, "value");

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonArray", result instanceof JsonArray);
  }

  @Test
  public void testConvertToClientCallableResult_JsonBoolean() {
    JsonBoolean input = Json.create(true);

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonBoolean", result instanceof JsonBoolean);
  }

  @Test
  public void testConvertToClientCallableResult_JsonString() {
    JsonString input = Json.create("test");

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonString", result instanceof JsonString);
  }

  @Test
  public void testConvertToClientCallableResult_JsonNumber() {
    JsonNumber input = Json.create(42);

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonNumber", result instanceof JsonNumber);
  }

  @Test
  public void testConvertToClientCallableResult_JsonNull() {
    JsonNull input = Json.createNull();

    JsonValue result = helper.convertToClientCallableResult(input);

    assertTrue("Result should be an instance of JsonNull", result instanceof JsonNull);
  }

}
