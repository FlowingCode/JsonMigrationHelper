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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.server.Version;
import elemental.json.JsonArray;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Test;

public class LitRendererMigrationExtensionTest {

  @Test
  public void testWithFunctionRegistersHandler() throws Exception {
    // Vaadin 23's LitRenderer.of() reads UI.getCurrent(); 24+ does not. Create a UI only on 23 so
    // that 24/25 never load the UI class (and so don't need the servlet API on the test classpath).
    boolean needsUi = Version.getMajorVersion() < 24;
    if (needsUi) {
      UI.setCurrent(new UI());
    }
    try {
      LitRenderer<String> renderer = LitRenderer.of("<div></div>");

      SerializableBiConsumer<String, JsonArray> handler = (source, array) -> {};

      LitRenderer<String> result =
          LitRendererMigrationExtension.withFunction(renderer, "click", handler);

      assertSame(renderer, result);
      assertNotNull("Handler for 'click' should be registered on the renderer",
          findRegisteredHandler(renderer, "click"));
    } finally {
      if (needsUi) {
        UI.setCurrent(null);
      }
    }
  }

  private static Object findRegisteredHandler(Object renderer, String functionName)
      throws Exception {
    for (Class<?> c = renderer.getClass(); c != null; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        if (Map.class.isAssignableFrom(field.getType())) {
          field.setAccessible(true);
          Map<?, ?> map = (Map<?, ?>) field.get(renderer);
          if (map != null && map.containsKey(functionName)) {
            return map.get(functionName);
          }
        }
      }
    }
    return null;
  }
}
