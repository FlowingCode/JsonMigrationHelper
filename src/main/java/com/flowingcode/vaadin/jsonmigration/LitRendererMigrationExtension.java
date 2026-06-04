package com.flowingcode.vaadin.jsonmigration;

import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.server.Version;
import elemental.json.JsonArray;
import lombok.SneakyThrows;

/**
 * Provides migration support for {@code LitRenderer} event handler registration across 
 * Vaadin versions.
 *
 * <p>In Vaadin 24, {@code LitRenderer.withFunction} receives a {@code JsonArray} from the
 * elemental.json API directly. In Vaadin 25+, the argument type changed to a Jackson
 * {@code ArrayNode}, so this class wraps the handler to perform the necessary conversion
 * transparently.
 *
 * <p>The {@code LitRenderer} type referenced here is resolved at runtime via a method handle,
 * allowing this library to compile against Vaadin 14 while supporting Vaadin 24+.
 *
 * <p><b>Note:</b> This class is only useful when running on Vaadin 24 or later. On earlier
 * versions, {@code LitRenderer} is not available and any attempt to use this class will fail at
 * runtime.
 */
public class LitRendererMigrationExtension {

  private class LitRenderer<SOURCE> {
    LitRenderer<SOURCE> withFunction(String functionName,
        SerializableBiConsumer<SOURCE, ?> handler) {
      return null;
    }
  }
 
  /**
   * Registers an event handler function on the given {@code LitRenderer}.
   *
   * <p>On Vaadin 25+, the raw argument passed by the client is a Jackson {@code ArrayNode} rather
   * than a {@code JsonArray}, so the handler is wrapped to convert it before forwarding.
   *
   * @param <SOURCE> the bean type of the renderer
   * @param renderer the {@code LitRenderer} on which to register the function
   * @param name the client-side function name
   * @param handler the handler to invoke when the client calls the function; receives the item and
   *     a {@code JsonArray} of arguments
   * @return the same renderer instance, for chaining
   */
  @SneakyThrows
  public static <SOURCE> LitRenderer<SOURCE> withFunction(LitRenderer<SOURCE> renderer, String name,
      SerializableBiConsumer<SOURCE, JsonArray> handler) {
    SerializableBiConsumer<SOURCE, ?> c = handler;
    if (Version.getMajorVersion() >= 25) {
      c = (source, array) -> handler.accept(source,
          (JsonArray) JsonMigration.convertToJsonValue(array));
    }
    return renderer.withFunction(name, c);
  }
  
}
