package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonString;

public class ClientCallable__JsonString extends BaseClientCallable {

    @ClientCallable
    public JsonString test() {
        trace();
        return Json.create("");
    }
}


