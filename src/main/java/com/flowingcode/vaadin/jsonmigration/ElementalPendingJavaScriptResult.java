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
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonCodec;
import elemental.json.JsonValue;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

/**
 * A pending result from a JavaScript snippet sent to the browser for evaluation. This interface
 * utilizes Elemental JSON in order to abstract away breaking changes introduced in Vaadin version
 * 25.
 *
 * @see PendingJavaScriptResult
 * @author Vaadin Ltd
 */
public interface ElementalPendingJavaScriptResult extends Serializable {

    /**
     * Adds an untyped handler that will be run for a successful execution and a
     * handler that will be run for a failed execution. One of the handlers will
     * be invoked asynchronously when the result of the execution is sent back
     * to the server. It is not possible to synchronously wait for the result of
     * the execution while holding the session lock since the request handling
     * thread that makes the result available will also need to lock the
     * session.
     * <p>
     * Handlers can only be added before the execution has been sent to the
     * browser.
     *
     * @param resultHandler
     *            a handler for the JSON representation of the value from a
     *            successful execution, not <code>null</code>
     * @param errorHandler
     *            a handler for an error message in case the execution failed,
     *            or <code>null</code> to ignore errors
     */
    void then(SerializableConsumer<JsonValue> resultHandler,
            SerializableConsumer<String> errorHandler);

    /**
     * Adds an untyped handler that will be run for a successful execution. The
     * handler will be invoked asynchronously if the execution was successful.
     * In case of a failure, no handler will be run.
     * <p>
     * A handler can only be added before the execution has been sent to the
     * browser.
     *
     * @param resultHandler
     *            a handler for the JSON representation of the return value from
     *            a successful execution, not <code>null</code>
     */
    default void then(SerializableConsumer<JsonValue> resultHandler) {
        then(resultHandler, null);
    }

    /**
     * Adds a typed handler that will be run for a successful execution and a
     * handler that will be run for a failed execution. One of the handlers will
     * be invoked asynchronously when the result of the execution is sent back
     * to the server.
     * <p>
     * Handlers can only be added before the execution has been sent to the
     * browser.
     *
     * @param targetType
     *            the type to convert the JavaScript return value to, not
     *            <code>null</code>
     * @param resultHandler
     *            a handler for the return value from a successful execution,
     *            not <code>null</code>
     * @param errorHandler
     *            a handler for an error message in case the execution failed,
     *            or <code>null</code> to ignore errors
     */
    default <T> void then(Class<T> targetType,
            SerializableConsumer<T> resultHandler,
            SerializableConsumer<String> errorHandler) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null");
        }
        if (resultHandler == null) {
            throw new IllegalArgumentException("Result handler cannot be null");
        }

        SerializableConsumer<JsonValue> convertingResultHandler = value -> resultHandler
                .accept(JsonCodec.decodeAs(value, targetType));

        then(convertingResultHandler, errorHandler);
    }

    /**
     * Adds a typed handler that will be run for a successful execution. The
     * handler will be invoked asynchronously if the execution was successful.
     * In case of a failure, no handler will be run.
     * <p>
     * A handler can only be added before the execution has been sent to the
     * browser.
     *
     * @param targetType
     *            the type to convert the JavaScript return value to, not
     *            <code>null</code>
     * @param resultHandler
     *            a handler for the return value from a successful execution,
     *            not <code>null</code>
     */
    default <T> void then(Class<T> targetType,
            SerializableConsumer<T> resultHandler) {
        then(targetType, resultHandler, null);
    }

    /**
     * Creates a typed completable future that will be completed with the result
     * of the execution. It will be completed asynchronously when the result of
     * the execution is sent back to the server. It is not possible to
     * synchronously wait for the result of the execution while holding the
     * session lock since the request handling thread that makes the result
     * available will also need to lock the session.
     * <p>
     * A completable future can only be created before the execution has been
     * sent to the browser.
     *
     * @param targetType
     *            the type to convert the JavaScript return value to, not
     *            <code>null</code>
     *
     * @return a completable future that will be completed based on the
     *         execution results, not <code>null</code>
     */
    <T> CompletableFuture<T> toCompletableFuture(Class<T> targetType);

    /**
     * Creates an untyped completable future that will be completed with the
     * result of the execution. It will be completed asynchronously when the
     * result of the execution is sent back to the server.
     * <p>
     * A completable future can only be created before the execution has been
     * sent to the browser.
     *
     * @return a completable future that will be completed based on the
     *         execution results, not <code>null</code>
     */
    default CompletableFuture<JsonValue> toCompletableFuture() {
        return toCompletableFuture(JsonValue.class);
    }

}
