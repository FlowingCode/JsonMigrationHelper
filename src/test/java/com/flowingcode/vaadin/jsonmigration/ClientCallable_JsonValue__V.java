package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonValue;

public class ClientCallable_JsonValue__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonValue arg) {
        trace();
    }
}


