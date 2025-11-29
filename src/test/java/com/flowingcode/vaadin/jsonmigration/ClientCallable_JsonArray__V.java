package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonArray;

public class ClientCallable_JsonArray__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonArray arg) {
        trace();
    }
}


