package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonBoolean;

public class ClientCallable_JsonBoolean__V extends BaseClientCallable {

  @ClientCallable
  public void test(JsonBoolean arg) {
    trace();
  }
}


