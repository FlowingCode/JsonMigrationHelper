package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonObject;

public class ClientCallable_JsonObject__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonObject arg) {
        trace();
    }
}


