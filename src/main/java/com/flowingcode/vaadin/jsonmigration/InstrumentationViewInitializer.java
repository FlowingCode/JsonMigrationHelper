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
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServiceInitListener;

/**
 * Abstract base class for Vaadin service initializers that register instrumented views. Subclasses
 * should implement {@link #serviceInit(com.vaadin.flow.server.ServiceInitEvent)} and call
 * {@link #registerInstrumentedRoute(Class)} to register views with instrumented routes.
 *
 * @author Javier Godoy / Flowing Code
 */
@SuppressWarnings("serial")
public abstract class InstrumentationViewInitializer implements VaadinServiceInitListener {

  /**
   * Registers an instrumented route for the given navigation target. The navigation target must be
   * annotated with {@link InstrumentedRoute} to specify the route path. This method calls
   * {@link JsonMigration#instrumentClass(Class)} to get the instrumented class and registers it as
   * a Vaadin view with the route derived from the annotation.
   *
   * @param navigationTarget the component class to instrument and register, must be annotated with
   *        {@link InstrumentedRoute}
   * @throws IllegalArgumentException if the navigationTarget is not annotated with
   *         {@link InstrumentedRoute}
   */
  protected final void registerInstrumentedRoute(Class<? extends Component> navigationTarget) {
    InstrumentedRoute annotation = navigationTarget.getAnnotation(InstrumentedRoute.class);
    if (annotation == null) {
      throw new IllegalArgumentException(
          navigationTarget.getName() + " must be annotated with @"
              + InstrumentedRoute.class.getSimpleName());
    }

    String route = annotation.value();
    navigationTarget = JsonMigration.instrumentClass(navigationTarget);
    RouteConfiguration.forApplicationScope().setRoute(route, navigationTarget);
  }

}
