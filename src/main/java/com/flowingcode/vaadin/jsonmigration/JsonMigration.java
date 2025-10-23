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

import java.lang.reflect.Method;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.Version;
import elemental.json.JsonValue;
import lombok.SneakyThrows;

/**
 * Provides a compatibility layer for JSON handling to abstract away breaking changes
 * introduced in Vaadin version 25.
 * <p>
 * This utility class detects the runtime version and uses version-specific helpers 
 * to ensure that code calling its methods does not need to be aware of underlying 
 * Vaadin API changes.
 * 
 * @author Javier Godoy
 */
public class JsonMigration {

  private static final JsonMigrationHelper helper = initializeHelper();
   
  @SneakyThrows
  private static JsonMigrationHelper initializeHelper() {
    if (Version.getMajorVersion()>24) {
      Class<?> helperType = Class.forName(JsonMigration.class.getName()+"Helper25"); 
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
   * Converts a given Java object into a {@code JsonValue}.
   *
   * <p>This method delegates the conversion to a version-specific helper to handle
   * any differences in the serialization process.
   *
   * @param object the object to convert
   * @return the {@code JsonValue} representation of the object
   */
  public static JsonValue convertToJsonValue(Object object) {
    return helper.convertToJsonValue(object);
  }

  @SneakyThrows
  private static Object invoke(Method method, Object instance, Object... args) {
    return helper.invoke(method, instance, args);
  }
  
  
  private static Method Element_setPropertyJson = lookup_setPropertyJson();

  @SneakyThrows
  private static Method lookup_setPropertyJson() {
    if (Version.getMajorVersion()>24) {
      return Element.class.getMethod("setPropertyJson", String.class, BASE_JSON_NODE);
    } else {
      return Element.class.getMethod("setPropertyJson", String.class, JsonValue.class);
    }
  }

  /**
   * Sets a JSON-valued property on a given {@code Element}, transparently handling
   * version-specific method signatures.
   *
   * <p>This method uses reflection to call the appropriate {@code setPropertyJson} method
   * on the {@code Element} class, which has a different signature for its JSON
   * parameter in library versions before and after Vaadin 25.
   *
   * @param element the {@code Element} on which to set the property
   * @param name    the name of the property to set
   * @param json    the {@code JsonValue} to be set as the property's value
   */
  public static void setPropertyJson(Element element, String name, JsonValue json) {
    invoke(Element_setPropertyJson, element, name, json);
  }

}
