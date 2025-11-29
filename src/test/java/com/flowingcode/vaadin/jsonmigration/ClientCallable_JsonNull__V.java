package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonNull;

public class ClientCallable_JsonNull__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonNull arg) {
        trace();
    }
}


