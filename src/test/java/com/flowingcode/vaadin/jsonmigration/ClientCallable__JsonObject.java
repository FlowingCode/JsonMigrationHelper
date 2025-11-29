package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonObject;

public class ClientCallable__JsonObject extends BaseClientCallable {

    @ClientCallable
    public JsonObject test() {
        trace();
        return Json.createObject();
    }
}


