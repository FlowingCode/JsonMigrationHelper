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

/*
 * Copyright 2000-2020 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.vaadin.flow.component.Component;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.ReflectTools;
import elemental.json.Json;
import elemental.json.JsonType;
import elemental.json.JsonValue;

/**
 * Utility for encoding objects to and from JSON.
 *
 * <p>Supported types are
 *
 * <ul>
 *   <li>{@link String}
 *   <li>{@link Boolean} and <code>boolean</code>
 *   <li>{@link Integer} and <code>int</code>
 *   <li>{@link Double} and <code>double</code> (<code>NaN</code> and infinity not supported)
 *   <li>{@link JsonValue} and all its sub types
 *   <li>{@link Element} (encoded as a reference to the element)
 *   <li>{@link Component} (encoded as a reference to the root element)
 * </ul>
 *
 * <p>
 *
 * @author Vaadin Ltd
 */
public class JsonCodec {

  /**
   * Decodes the given JSON value as the given type.
   *
   * <p>Supported types are {@link String}, {@link Boolean}, {@link Integer}, {@link Double} and
   * primitives boolean, int, double
   *
   * @param <T> the decoded type
   * @param json the JSON value
   * @param type the type to decode as
   * @return the value decoded as the given type
   * @throws IllegalArgumentException if the type was unsupported
   */
  @SuppressWarnings("unchecked")
  public static <T> T decodeAs(JsonValue json, Class<T> type) {
    assert json != null;
    if (json.getType() == JsonType.NULL && !type.isPrimitive()) {
      return null;
    }
    Class<?> convertedType = ReflectTools.convertPrimitiveType(type);
    if (type == String.class) {
      return type.cast(json.asString());
    } else if (convertedType == Boolean.class) {
      return (T) convertedType.cast(Boolean.valueOf(json.asBoolean()));
    } else if (convertedType == Double.class) {
      return (T) convertedType.cast(Double.valueOf(json.asNumber()));
    } else if (convertedType == Integer.class) {
      return (T) convertedType.cast(Integer.valueOf((int) json.asNumber()));
    } else if (JsonValue.class.isAssignableFrom(type)) {
      return type.cast(json);
    } else {
      throw new IllegalArgumentException("Unknown type " + type.getName());
    }
  }

  /**
   * Helper for checking whether the type is supported by {@link #encodeWithoutTypeInfo(Object)}.
   * Supported value types are {@link String}, {@link Integer}, {@link Double}, {@link Boolean},
   * {@link JsonValue}.
   *
   * @param type the type to check
   * @return whether the type can be encoded
   */
  public static boolean canEncodeWithoutTypeInfo(Class<?> type) {
    assert type != null;
    return String.class.equals(type)
        || Integer.class.equals(type)
        || Double.class.equals(type)
        || Boolean.class.equals(type)
        || JsonValue.class.isAssignableFrom(type);
  }

  /**
   * Helper for encoding any "primitive" value that is directly supported in JSON. Supported values
   * types are {@link String}, {@link Number}, {@link Boolean}, {@link JsonValue}. <code>null</code>
   * is also supported.
   *
   * @param value the value to encode
   * @return the value encoded as JSON
   */
  public static JsonValue encodeWithoutTypeInfo(Object value) {
    if (value == null) {
      return Json.createNull();
    }

    assert canEncodeWithoutTypeInfo(value.getClass());

    Class<?> type = value.getClass();
    if (String.class.equals(value.getClass())) {
      return Json.create((String) value);
    } else if (Integer.class.equals(type) || Double.class.equals(type)) {
      return Json.create(((Number) value).doubleValue());
    } else if (Boolean.class.equals(type)) {
      return Json.create(((Boolean) value).booleanValue());
    } else if (JsonValue.class.isAssignableFrom(type)) {
      return (JsonValue) value;
    }
    assert !canEncodeWithoutTypeInfo(type);
    throw new IllegalArgumentException("Can't encode " + value.getClass() + " to json");
  }
}
