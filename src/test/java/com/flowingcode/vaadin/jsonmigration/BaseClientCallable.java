package com.flowingcode.vaadin.jsonmigration;

import com.vaadin.flow.component.html.Div;

public abstract class BaseClientCallable extends Div {

  private boolean traced;

  protected final void trace() {
    traced = true;
  }

  public boolean hasBeenTraced() {
    return traced;
  }

}
