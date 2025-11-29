package com.flowingcode.vaadin.jsonmigration;

import com.vaadin.flow.component.Component;
import elemental.json.Json;

public class LegacyClientCallablesTest24 extends LegacyClientCallablesTest {

  @Override
  protected <T extends Component> Class<? extends T> instrumentClass(Class<T> clazz) {
    return new LegacyJsonMigrationHelper().instrumentClass(clazz);
  }

  @Override
  protected Object createJsonNull() {
    return Json.createNull();
  }

  @Override
  protected Object createJsonBoolean() {
    return Json.create(true);
  }

  @Override
  protected Object createJsonNumber() {
    return Json.create(42);
  }

  @Override
  protected Object createJsonString() {
    return Json.create("test");
  }

  @Override
  protected Object createJsonArray() {
    return Json.createArray();
  }

  @Override
  protected Object createJsonObject() {
    return Json.createObject();
  }


}
