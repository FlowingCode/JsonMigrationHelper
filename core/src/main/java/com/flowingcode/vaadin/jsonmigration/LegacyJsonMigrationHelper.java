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
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import elemental.json.JsonValue;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

@NoArgsConstructor
class LegacyJsonMigrationHelper implements JsonMigrationHelper {

  private static final ClassInstrumentationUtil instrumentation = new ClassInstrumentationUtil(24);

  @Override
  public <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    return instrumentation.instrumentClass(clazz);
  }

  @Override
  public JsonValue convertToJsonValue(Object object) {
    if (object instanceof JsonValue) {
      return (JsonValue) object;
    } else {
      throw new ClassCastException(
          object.getClass().getName() + " cannot be converted to elemental.json.JsonObject");
    }
  }

  @Override
  public <T extends JsonValue> T convertToClientCallableResult(T object) {
    return object;
  }

  @Override
  @SneakyThrows
  public Object invoke(Method method, Object instance, Object... args) {
    return method.invoke(instance, args);
  }

  @Override
  public ElementalPendingJavaScriptResult convertPendingJavaScriptResult(
      PendingJavaScriptResult result) {
    return new PendingJavaScriptResultImpl(result);
  }

  @SuppressWarnings("serial")
  @AllArgsConstructor
  private static final class PendingJavaScriptResultImpl
      implements ElementalPendingJavaScriptResult, PendingJavaScriptResult {
    @Delegate private final PendingJavaScriptResult delegate;
  }
}
