package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonNumber;

public class ClientCallable_JsonNumber__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonNumber arg) {
        trace();
    }
}


