package com.flowingcode.vaadin.jsonmigration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import elemental.json.JsonValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.Test;

public abstract class LegacyClientCallablesPrivateTest {

  protected abstract <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz);

  private static final String MESSAGE = "instrumented method is not annotated with @ClientCallable";

  /**
   * Finds the test method declared in the instrumented class that is annotated
   * with @ClientCallable.
   *
   * @param c the instrumented callable instance
   * @return the Method if found, null otherwise
   */
  private static Method getAnnotatedTestMethod(BaseClientCallable c, Class<? extends Annotation> annotationType) {
    for (Method method : c.getClass().getDeclaredMethods()) {
      if ("test".equals(method.getName()) && method.isAnnotationPresent(annotationType)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Invokes the test method on the instrumented instance via reflection.
   *
   * @param instrumented the instrumented instance
   * @param args         the arguments to pass to the test method
   * @return the result of the method invocation
   * @throws Exception if invocation fails
   */
  private static Object invokeTestMethod(BaseClientCallable instrumented, Object... args) throws Exception {
    Method testMethod = getAnnotatedTestMethod(instrumented, ClientCallable.class);
    assertTrue(MESSAGE, testMethod != null);
    testMethod.setAccessible(true);
    return testMethod.invoke(instrumented, args);
  }

  private static Object invokeLegacyMethod(BaseClientCallable instrumented, Object... args) throws Exception {
    Method testMethod = getAnnotatedTestMethod(instrumented, LegacyClientCallable.class);
    testMethod.setAccessible(true);
    return testMethod.invoke(instrumented, args);
  }

  protected abstract Object createJsonNull();

  protected abstract Object createJsonBoolean();

  protected abstract Object createJsonNumber();

  protected abstract Object createJsonString();

  protected abstract Object createJsonArray();

  protected abstract Object createJsonObject();

  @Test
  public void testExtends__V() throws Exception {
    LegacyClientCallablePrivate__V instrumented =
        instrumentClass(ExtendsLegacyClientCallablePrivate__V.class).getDeclaredConstructor()
            .newInstance();
    invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test__V() throws Exception {
    LegacyClientCallablePrivate__V instrumented = instrumentClass(LegacyClientCallablePrivate__V.class).getDeclaredConstructor()
        .newInstance();
    invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_Z__V() throws Exception {
    LegacyClientCallablePrivate_Z__V instrumented = instrumentClass(LegacyClientCallablePrivate_Z__V.class).getDeclaredConstructor()
        .newInstance();
    invokeTestMethod(instrumented, true);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_I__V() throws Exception {
    LegacyClientCallablePrivate_I__V instrumented = instrumentClass(LegacyClientCallablePrivate_I__V.class).getDeclaredConstructor()
        .newInstance();
    invokeTestMethod(instrumented, 42);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_D__V() throws Exception {
    LegacyClientCallablePrivate_D__V instrumented = instrumentClass(LegacyClientCallablePrivate_D__V.class).getDeclaredConstructor()
        .newInstance();
    invokeTestMethod(instrumented, 3.14);
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_String__V() throws Exception {
    LegacyClientCallablePrivate_String__V instrumented = instrumentClass(LegacyClientCallablePrivate_String__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, "test");
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test__Z() throws Exception {
    LegacyClientCallablePrivate__Z instrumented = instrumentClass(LegacyClientCallablePrivate__Z.class).getDeclaredConstructor()
        .newInstance();
    LegacyClientCallablePrivate__Z nonInstrumented = new LegacyClientCallablePrivate__Z();
    boolean result = (boolean) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(invokeLegacyMethod(nonInstrumented), result);
  }

  @Test
  public void test__I() throws Exception {
    LegacyClientCallablePrivate__I instrumented = instrumentClass(LegacyClientCallablePrivate__I.class).getDeclaredConstructor()
        .newInstance();
    LegacyClientCallablePrivate__I nonInstrumented = new LegacyClientCallablePrivate__I();
    int result = (int) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(invokeLegacyMethod(nonInstrumented), result);
  }

  @Test
  public void test__D() throws Exception {
    LegacyClientCallablePrivate__D instrumented = instrumentClass(LegacyClientCallablePrivate__D.class).getDeclaredConstructor()
        .newInstance();
    LegacyClientCallablePrivate__D nonInstrumented = new LegacyClientCallablePrivate__D();
    double result = (double) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals((double) invokeLegacyMethod(nonInstrumented), result, 0.0);
  }

  @Test
  public void test__Integer() throws Exception {
    LegacyClientCallablePrivate__Integer instrumented = instrumentClass(LegacyClientCallablePrivate__Integer.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__Integer nonInstrumented = new LegacyClientCallablePrivate__Integer();
    Integer result = (Integer) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(invokeLegacyMethod(nonInstrumented), result);
  }

  @Test
  public void test__JsonValue() throws Exception {
    LegacyClientCallablePrivate__JsonValue instrumented = instrumentClass(LegacyClientCallablePrivate__JsonValue.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonValue nonInstrumented = new LegacyClientCallablePrivate__JsonValue();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonBoolean() throws Exception {
    LegacyClientCallablePrivate__JsonBoolean instrumented = instrumentClass(LegacyClientCallablePrivate__JsonBoolean.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonBoolean nonInstrumented = new LegacyClientCallablePrivate__JsonBoolean();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonNumber() throws Exception {
    LegacyClientCallablePrivate__JsonNumber instrumented = instrumentClass(LegacyClientCallablePrivate__JsonNumber.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonNumber nonInstrumented = new LegacyClientCallablePrivate__JsonNumber();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonString() throws Exception {
    LegacyClientCallablePrivate__JsonString instrumented = instrumentClass(LegacyClientCallablePrivate__JsonString.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonString nonInstrumented = new LegacyClientCallablePrivate__JsonString();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonNull() throws Exception {
    LegacyClientCallablePrivate__JsonNull instrumented = instrumentClass(LegacyClientCallablePrivate__JsonNull.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonNull nonInstrumented = new LegacyClientCallablePrivate__JsonNull();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonArray() throws Exception {
    LegacyClientCallablePrivate__JsonArray instrumented = instrumentClass(LegacyClientCallablePrivate__JsonArray.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonArray nonInstrumented = new LegacyClientCallablePrivate__JsonArray();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test__JsonObject() throws Exception {
    LegacyClientCallablePrivate__JsonObject instrumented = instrumentClass(LegacyClientCallablePrivate__JsonObject.class)
        .getDeclaredConstructor().newInstance();
    LegacyClientCallablePrivate__JsonObject nonInstrumented = new LegacyClientCallablePrivate__JsonObject();
    JsonValue result = (JsonValue) invokeTestMethod(instrumented);
    assertTrue(instrumented.hasBeenTraced());
    assertEquals(((JsonValue) invokeLegacyMethod(nonInstrumented)).toJson(), result.toJson());
  }

  @Test
  public void test_JsonValue__V() throws Exception {
    LegacyClientCallablePrivate_JsonValue__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonValue__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonNull());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonBoolean__V() throws Exception {
    LegacyClientCallablePrivate_JsonBoolean__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonBoolean__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonBoolean());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonNumber__V() throws Exception {
    LegacyClientCallablePrivate_JsonNumber__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonNumber__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonNumber());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonString__V() throws Exception {
    LegacyClientCallablePrivate_JsonString__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonString__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonString());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonNull__V() throws Exception {
    LegacyClientCallablePrivate_JsonNull__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonNull__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonNull());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonArray__V() throws Exception {
    LegacyClientCallablePrivate_JsonArray__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonArray__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonArray());
    assertTrue(instrumented.hasBeenTraced());
  }

  @Test
  public void test_JsonObject__V() throws Exception {
    LegacyClientCallablePrivate_JsonObject__V instrumented = instrumentClass(LegacyClientCallablePrivate_JsonObject__V.class)
        .getDeclaredConstructor().newInstance();
    invokeTestMethod(instrumented, createJsonObject());
    assertTrue(instrumented.hasBeenTraced());
  }

}
