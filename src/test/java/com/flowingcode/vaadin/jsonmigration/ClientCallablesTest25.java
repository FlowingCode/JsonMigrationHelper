package com.flowingcode.vaadin.jsonmigration;


import static org.hamcrest.Matchers.containsString;
import com.vaadin.flow.component.Component;
import elemental.json.JsonValue;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public class ClientCallablesTest25 extends ClientCallablesTest {

  private static final String ERRMSG = "must be annotated with @LegacyClientCallable";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Override
  protected <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    for (Class<?> arg : getClientCallableTestMethod(clazz).getParameterTypes()) {
      if (JsonValue.class.isAssignableFrom(arg)) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(containsString(ERRMSG));
        break;
      }
    }
    return new JsonMigrationHelper25().instrumentClass(clazz);
  }

  @Override
  protected Object createJsonNull() {
    return NullNode.getInstance();
  }

  @Override
  protected Object createJsonBoolean() {
    return BooleanNode.TRUE;
  }

  @Override
  protected Object createJsonNumber() {
    return DoubleNode.valueOf(42);
  }

  @Override
  protected Object createJsonString() {
    return StringNode.valueOf("test");
  }

  @Override
  protected Object createJsonArray() {
    return new ArrayNode(JsonNodeFactory.instance);
  }

  @Override
  protected Object createJsonObject() {
    return new ObjectNode(JsonNodeFactory.instance);
  }

}


