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

import com.vaadin.flow.component.page.PendingJavaScriptResult;
import elemental.json.JsonValue;

import java.lang.reflect.Method;
import com.vaadin.flow.dom.Element;

interface JsonMigrationHelper {

  JsonValue convertToJsonValue(Object object);

  Object convertToClientCallableResult(Object object);

  Object invoke(Method method, Object instance, Object... args);

  ElementalPendingJavaScriptResult convertPendingJavaScriptResult(PendingJavaScriptResult result);

}
