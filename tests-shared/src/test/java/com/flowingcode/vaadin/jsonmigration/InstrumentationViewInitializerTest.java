/*-
 * #%L
 * Json Migration Helper
 * %%
 * Copyright (C) 2025-2026 Flowing Code
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import elemental.json.JsonValue;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * Tests for the parent layout chain derived from {@link InstrumentedRoute#layout()} by {@link
 * InstrumentationViewInitializer}.
 */
public class InstrumentationViewInitializerTest {

  public static class RootLayout extends Div implements RouterLayout {}

  @ParentLayout(RootLayout.class)
  public static class MiddleLayout extends Div implements RouterLayout {}

  @ParentLayout(MiddleLayout.class)
  public static class LeafLayout extends Div implements RouterLayout {}

  public static class CallableLayout extends Div implements RouterLayout {
    @LegacyClientCallable
    protected JsonValue test(JsonValue value) {
      return value;
    }
  }

  public static class NonComponentLayout implements RouterLayout {
    @Override
    public Element getElement() {
      return null;
    }
  }

  @InstrumentedRoute("without-layout")
  public static class RouteWithoutLayout extends Div {}

  @InstrumentedRoute(value = "with-layout", layout = LeafLayout.class)
  public static class RouteWithLayout extends Div {}

  @Route("already-annotated")
  @InstrumentedRoute("already-annotated-instrumented")
  public static class RouteAlreadyAnnotated extends Div {}

  @Test
  public void testLayoutDefaultsToUI() {
    InstrumentedRoute annotation = RouteWithoutLayout.class.getAnnotation(InstrumentedRoute.class);
    assertEquals(UI.class, annotation.layout());
  }

  @Test
  public void testDefaultLayoutYieldsEmptyParentChain() {
    InstrumentedRoute annotation = RouteWithoutLayout.class.getAnnotation(InstrumentedRoute.class);
    assertTrue(InstrumentationViewInitializer.getParentChain(annotation.layout()).isEmpty());
  }

  @Test
  public void testSingleLayout() {
    List<Class<? extends RouterLayout>> chain =
        InstrumentationViewInitializer.getParentChain(RootLayout.class);
    assertEquals(Arrays.asList(JsonMigration.instrumentClass(RootLayout.class)), chain);
  }

  @Test
  public void testParentLayoutChainIsWalked() {
    InstrumentedRoute annotation = RouteWithLayout.class.getAnnotation(InstrumentedRoute.class);
    List<Class<? extends RouterLayout>> chain =
        InstrumentationViewInitializer.getParentChain(annotation.layout());
    assertEquals(
        Arrays.asList(
            JsonMigration.instrumentClass(LeafLayout.class),
            JsonMigration.instrumentClass(MiddleLayout.class),
            JsonMigration.instrumentClass(RootLayout.class)),
        chain);
  }

  @Test
  public void testLayoutIsInstrumented() {
    List<Class<? extends RouterLayout>> chain =
        InstrumentationViewInitializer.getParentChain(CallableLayout.class);
    assertEquals(1, chain.size());
    Class<? extends RouterLayout> instrumented = chain.get(0);
    assertEquals(JsonMigration.instrumentClass(CallableLayout.class), instrumented);
    assertTrue(CallableLayout.class.isAssignableFrom(instrumented));
    assertTrue(RouterLayout.class.isAssignableFrom(instrumented));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonComponentLayoutThrows() {
    InstrumentationViewInitializer.getParentChain(NonComponentLayout.class);
  }

  @Test
  public void testRouteAnnotationIsAdded() {
    Class<?> instrumented = JsonMigration.instrumentClass(RouteWithoutLayout.class);
    assertNotEquals(RouteWithoutLayout.class, instrumented);
    Route route = instrumented.getAnnotation(Route.class);
    assertNotNull(route);
    assertEquals("without-layout", route.value());
    assertEquals(UI.class, route.layout());
    assertFalse(route.registerAtStartup());
  }

  @Test
  public void testRouteAnnotationPropagatesLayout() {
    Class<?> instrumented = JsonMigration.instrumentClass(RouteWithLayout.class);
    Route route = instrumented.getAnnotation(Route.class);
    assertNotNull(route);
    assertEquals("with-layout", route.value());
    assertEquals(LeafLayout.class, route.layout());
    assertFalse(route.registerAtStartup());
  }

  @Test
  public void testExistingRouteAnnotationIsNotReplaced() {
    assertEquals(
        RouteAlreadyAnnotated.class, JsonMigration.instrumentClass(RouteAlreadyAnnotated.class));
  }
}
