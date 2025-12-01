package com.flowingcode.vaadin.jsonmigration;

import com.vaadin.flow.component.Component;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

// this test doesn't work as-in because it requires Vaadin 25 in the classpath
public class LegacyClientCallablesPrivateTest25 extends LegacyClientCallablesPrivateTest {

  @Override
  protected <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
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
