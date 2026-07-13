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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a {@link Component} class for instrumented route registration.
 *
 * @author Javier Godoy / Flowing Code
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface InstrumentedRoute {

  /**
   * The route path for this component.
   *
   * @return the route path
   */
  String value();
  
  /**
   * Sets the parent component for the route target component.
   * <p>
   *
   * @return the layout component class used by the route target component.
   * @see Route#layout()
   */
  Class<? extends RouterLayout> layout() default UI.class;
}
