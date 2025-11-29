package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonNumber;

public class ClientCallable__JsonNumber extends BaseClientCallable {

    @ClientCallable
    public JsonNumber test() {
        trace();
        return Json.create(0);
    }
}


