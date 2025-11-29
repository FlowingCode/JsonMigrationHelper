package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.JsonString;

public class ClientCallable_JsonString__V extends BaseClientCallable {

    @ClientCallable
    public void test(JsonString arg) {
        trace();
    }
}


