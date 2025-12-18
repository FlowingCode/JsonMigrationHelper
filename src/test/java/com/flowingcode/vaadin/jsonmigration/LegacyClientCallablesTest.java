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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import elemental.json.JsonValue;
import java.lang.reflect.Method;
import org.junit.Test;

public abstract class LegacyClientCallablesTest {

  protected abstract <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz);

  private static final String MESSAGE = "instrumented method is not annotated with @ClientCallable";

  /**
   * Finds the test method declared in the instrumented class that is annotated
   * with @ClientCallable.
   *
   * @param c the instrumented callable instance
   * @return the Method if found, null otherwise
   */
  private static Method getClientCallableTestMethod(BaseClientCallable c) {
    for (Method method : c.getClass().getDeclaredMethods()) {
      if ("test".equals(method.getName()) && method.isAnnotationPresent(ClientCallable.class)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Invokes the test method on the instrumented instance via reflection.
   *
   * @param instrumented the instrumented instance
   * @param args the arguments to pass to the test method
   * @return the result of the method invocation
   * @throws Exception if invocation fails
   */
  private static Object invokeTestMethod(BaseClientCallable instrumented, Object... args)
      throws Exception {
    Method testMethod = getClientCallableTestMethod(instrumented);
    assertTrue(MESSAGE, testMethod != null);
    return testMethod.invoke(instrumented, args);
  }

  protected abstract Object createJsonNull();

  protected abstract Object createJsonBoolean();

  protected abstract Object createJsonNumber();

  protected abstract Object createJsonString();

  protected abstract Object createJsonArray();

  protected abstract Object createJsonObject();

  protected abstract Object createArrayOfJsonObject();

  protected abstract Object createArrayOfJsonString();

  @Test
  public void test__V() throws Exception {
    LegacyClientCallable__V instrumented =
        instrumentClass(LegacyClientCallable__V.class).getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_Z__V() throws Exception {
    LegacyClientCallable_Z__V instrumented =
        instrumentClass(LegacyClientCallable_Z__V.class).getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, true);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_I__V() throws Exception {
    LegacyClientCallable_I__V instrumented =
        instrumentClass(LegacyClientCallable_I__V.class).getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, 42);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_D__V() throws Exception {
    LegacyClientCallable_D__V instrumented =
        instrumentClass(LegacyClientCallable_D__V.class).getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, 3.14);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_String__V() throws Exception {
    LegacyClientCallable_String__V instrumented =
        instrumentClass(LegacyClientCallable_String__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, "test");
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test__Z() throws Exception {
    LegacyClientCallable__Z instrumented =
        instrumentClass(LegacyClientCallable__Z.class).getDeclaredConstructor().newInstance();
    LegacyClientCallable__Z nonInstrumented = new LegacyClientCallable__Z();
    boolean result = (boolean) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test(), result);
  }

  @Test
  public void test__I() throws Exception {
    LegacyClientCallable__I instrumented =
        instrumentClass(LegacyClientCallable__I.class).getDeclaredConstructor().newInstance();
    LegacyClientCallable__I nonInstrumented = new LegacyClientCallable__I();
    int result = (int) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test(), result);
  }

  @Test
  public void test__D() throws Exception {
    LegacyClientCallable__D instrumented =
        instrumentClass(LegacyClientCallable__D.class).getDeclaredConstructor().newInstance();
    LegacyClientCallable__D nonInstrumented = new LegacyClientCallable__D();
    double result = (double) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test(), result, 0.0);
  }

  @Test
  public void test__Integer() throws Exception {
    LegacyClientCallable__Integer instrumented =
        instrumentClass(LegacyClientCallable__Integer.class).getDeclaredConstructor().newInstance();
    LegacyClientCallable__Integer nonInstrumented = new LegacyClientCallable__Integer();
    Integer result = (Integer) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test(), result);
  }

  @Test
  public void test__JsonValue() throws Exception {
    LegacyClientCallable__JsonValue instrumented =
        instrumentClass(LegacyClientCallable__JsonValue.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonValue nonInstrumented = new LegacyClientCallable__JsonValue();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonBoolean() throws Exception {
    LegacyClientCallable__JsonBoolean instrumented =
        instrumentClass(LegacyClientCallable__JsonBoolean.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonBoolean nonInstrumented = new LegacyClientCallable__JsonBoolean();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonNumber() throws Exception {
    LegacyClientCallable__JsonNumber instrumented =
        instrumentClass(LegacyClientCallable__JsonNumber.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonNumber nonInstrumented = new LegacyClientCallable__JsonNumber();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonString() throws Exception {
    LegacyClientCallable__JsonString instrumented =
        instrumentClass(LegacyClientCallable__JsonString.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonString nonInstrumented = new LegacyClientCallable__JsonString();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonNull() throws Exception {
    LegacyClientCallable__JsonNull instrumented =
        instrumentClass(LegacyClientCallable__JsonNull.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonNull nonInstrumented = new LegacyClientCallable__JsonNull();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonArray() throws Exception {
    LegacyClientCallable__JsonArray instrumented =
        instrumentClass(LegacyClientCallable__JsonArray.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonArray nonInstrumented = new LegacyClientCallable__JsonArray();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test__JsonObject() throws Exception {
    LegacyClientCallable__JsonObject instrumented =
        instrumentClass(LegacyClientCallable__JsonObject.class)
            .getDeclaredConstructor()
            .newInstance();
    LegacyClientCallable__JsonObject nonInstrumented = new LegacyClientCallable__JsonObject();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(nonInstrumented.test().toJson(), result.toJson());
  }

  @Test
  public void test_JsonValue__V() throws Exception {
    LegacyClientCallable_JsonValue__V instrumented =
        instrumentClass(LegacyClientCallable_JsonValue__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonNull());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonBoolean__V() throws Exception {
    LegacyClientCallable_JsonBoolean__V instrumented =
        instrumentClass(LegacyClientCallable_JsonBoolean__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonBoolean());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonNumber__V() throws Exception {
    LegacyClientCallable_JsonNumber__V instrumented =
        instrumentClass(LegacyClientCallable_JsonNumber__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonNumber());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonString__V() throws Exception {
    LegacyClientCallable_JsonString__V instrumented =
        instrumentClass(LegacyClientCallable_JsonString__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonString());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonNull__V() throws Exception {
    LegacyClientCallable_JsonNull__V instrumented =
        instrumentClass(LegacyClientCallable_JsonNull__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonNull());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonArray__V() throws Exception {
    LegacyClientCallable_JsonArray__V instrumented =
        instrumentClass(LegacyClientCallable_JsonArray__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonArray());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonObject__V() throws Exception {
    LegacyClientCallable_JsonObject__V instrumented =
        instrumentClass(LegacyClientCallable_JsonObject__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createJsonObject());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_ArrayOfJsonString__V() throws Exception {
    LegacyClientCallable_ArrayOfJsonString__V instrumented =
        instrumentClass(LegacyClientCallable_ArrayOfJsonString__V.class).getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createArrayOfJsonString());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_ArrayOfJsonObject__V() throws Exception {
    LegacyClientCallable_ArrayOfJsonObject__V instrumented =
        instrumentClass(LegacyClientCallable_ArrayOfJsonObject__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createArrayOfJsonObject());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonObjectVarargs__V() throws Exception {
    LegacyClientCallable_JsonObjectVarargs__V instrumented =
        instrumentClass(LegacyClientCallable_JsonObjectVarargs__V.class)
            .getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented, createArrayOfJsonObject());
    assertTrue(instrumented.hasBeenTraced());
  }

}
