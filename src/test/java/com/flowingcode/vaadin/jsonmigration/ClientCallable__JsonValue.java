package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonValue;

public class ClientCallable__JsonValue extends BaseClientCallable {

    @ClientCallable
    public JsonValue test() {
        trace();
        return Json.createObject();
    }
}


