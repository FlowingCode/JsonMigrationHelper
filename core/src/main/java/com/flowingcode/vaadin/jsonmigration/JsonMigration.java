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

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Version;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.io.Serializable;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * Provides a compatibility layer for JSON handling to abstract away breaking changes introduced in
 * Vaadin version 25.
 *
 * <p>This utility class detects the runtime version and uses version-specific helpers to ensure
 * that code calling its methods does not need to be aware of underlying Vaadin API changes.
 *
 * @author Javier Godoy
 */
@UtilityClass
public class JsonMigration {

  private static final JsonMigrationHelper helper = initializeHelper();

  @SneakyThrows
  private static JsonMigrationHelper initializeHelper() {
    if (Version.getMajorVersion() > 24) {
      Class<?> helperType = Class.forName(JsonMigration.class.getName() + "Helper25");
      return (JsonMigrationHelper) helperType.getConstructor().newInstance();
    } else {
      return new LegacyJsonMigrationHelper();
    }
  }

  private static final Class<?> BASE_JSON_NODE = lookup_BaseJsonNode();

  private static Class<?> lookup_BaseJsonNode() {
    try {
      return Class.forName("tools.jackson.databind.node.BaseJsonNode");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Converts a given Java object into the return type of a {@link ClientCallable method}.
   *
   * <p>In Vaadin 25, this method converts {@code JsonValue} into {@code JsonNode}.
   *
   * @param object the object to convert
   * @return an {@code Object} suitable to use as the result of a {@code ClientCallable} method.
   */
  public static <T extends JsonValue> T convertToClientCallableResult(T object) {
    return helper.convertToClientCallableResult(object);
  }

  /**
   * Converts a given Java object into a {@code JsonValue}.
   *
   * <p>This method delegates the conversion to a version-specific helper to handle any differences
   * in the serialization process.
   *
   * @param object the object to convert
   * @return the {@code JsonValue} representation of the object
   */
  public static JsonValue convertToJsonValue(Object object) {
    return helper.convertToJsonValue(object);
  }

  /**
   * Converts an array of objects into an array of {@code JsonValue} objects.
   *
   * <p>
   * This method delegates the conversion to a version-specific helper to handle any differences in
   * the serialization process.
   *
   * @param source the array of objects to convert
   * @param target the destination array to be populated with converted {@code JsonValue}s
   * @throws NullPointerException if source or target is null
   * @throws IllegalArgumentException if the array lengths do not match
   * @throws ArrayStoreException if an element in the {@code source} array is converted into a type
   *         that is not assignable to the runtime component type of the {@code target} array
   */
  public static void convertToJsonValue(Object[] source, JsonValue[] target) {
    if (source.length != target.length) {
      throw new IllegalArgumentException(
          String.format("Array length mismatch: source.length=%d, target.length=%d",
              source.length, target.length));
    }

    for (int i = 0; i < target.length; i++) {
      target[i] = convertToJsonValue(source[i]);
    }
  }

  @SneakyThrows
  private static Object invoke(Method method, Object instance, Object... args) {
    return helper.invoke(method, instance, args);
  }

  private static Method Element_setPropertyJson = lookup_setPropertyJson();

  @SneakyThrows
  private static Method lookup_setPropertyJson() {
    if (Version.getMajorVersion() > 24) {
      return Element.class.getMethod("setPropertyJson", String.class, BASE_JSON_NODE);
    } else {
      return Element.class.getMethod("setPropertyJson", String.class, JsonValue.class);
    }
  }

  private static Method DomEvent_getEventData = lookup_getEventData();

  @SneakyThrows
  private static Method lookup_getEventData() {
    return DomEvent.class.getMethod("getEventData");
  }

  /**
   * Sets a JSON-valued property on a given {@code Element}, transparently handling version-specific
   * method signatures.
   *
   * <p>This method uses reflection to call the appropriate {@code setPropertyJson} method on the
   * {@code Element} class, which has a different signature for its JSON parameter in library
   * versions before and after Vaadin 25.
   *
   * @param element the {@code Element} on which to set the property
   * @param name the name of the property to set
   * @param json the {@code JsonValue} to be set as the property's value
   */
  public static void setPropertyJson(Element element, String name, JsonValue json) {
    invoke(Element_setPropertyJson, element, name, json);
  }

  private static Method Element_executeJs = lookup_executeJs();

  @SneakyThrows
  private static Method lookup_executeJs() {
    if (Version.getMajorVersion() > 24) {
      return Element.class.getMethod("executeJs", String.class, Object[].class);
    } else {
      return Element.class.getMethod("executeJs", String.class, Serializable[].class);
    }
  }

  /**
   * Asynchronously runs the given JavaScript expression in the browser in the context of this
   * element.
   *
   * @param element the {@code Element} on which to run the JavaScript expression
   * @param expression the JavaScript expression to invoke
   * @param parameters parameters to pass to the expression
   * @return a pending result that can be used to get a value returned from the expression
   * @see Element#executeJs(String, Serializable...)
   */
  public static ElementalPendingJavaScriptResult executeJs(
      Element element, String expression, Serializable... parameters) {
    PendingJavaScriptResult result =
        (PendingJavaScriptResult) invoke(Element_executeJs, element, expression, parameters);
    return helper.convertPendingJavaScriptResult(result);
  }

  /**
   * Gets additional data related to the event.
   *
   * @param event the {@code DomEvent} from which to retrieve the data
   * @return a JSON object containing event data, never <code>null</code>
   * @see DomEvent#getEventData()
   */
  public static JsonObject getEventData(DomEvent event) {
    return (JsonObject) convertToJsonValue(invoke(DomEvent_getEventData, event));
  }

  /**
   * Instruments a component class to ensure compatibility with Vaadin 25+ JSON handling changes in
   * {@link ClientCallable} methods.
   *
   * <p>This method creates a dynamically generated subclass of the given component class that
   * automatically converts {@code JsonValue} return values from {@link ClientCallable} methods to
   * the appropriate {@code JsonNode} type required by Vaadin 25+.
   *
   * <p><b>Behavior by Vaadin version:</b>
   *
   * <ul>
   *   <li><b>Vaadin 25+:</b> Returns an instrumented subclass that overrides all
   *       {@code @ClientCallable} methods returning {@code JsonValue} types. These overridden
   *       methods call the parent implementation and then convert the result through {@link
   *       #convertToClientCallableResult(JsonValue)} to ensure compatibility with the new
   *       Jackson-based JSON API.
   *   <li><b>Vaadin 24 and earlier:</b> Returns the original class unchanged, as no instrumentation
   *       is needed for the elemental.json API.
   * </ul>
   *
   * @param <T> the type of the component class
   * @param clazz the component class to instrument
   * @return in Vaadin 25+, an instrumented subclass of {@code clazz}; in earlier versions, the
   *     original {@code clazz}
   * @throws IllegalArgumentException if the class does not meet the requirements for
   *     instrumentation
   * @throws RuntimeException if the instrumentation fails
   * @see ClientCallable
   * @see InstrumentedRoute
   * @see #convertToClientCallableResult(JsonValue)
   */
  public static <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    return helper.instrumentClass(clazz);
  }
}
