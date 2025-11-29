package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonArray;

public class ClientCallable__JsonArray extends BaseClientCallable {

    @ClientCallable
    public JsonArray test() {
        trace();
        return Json.createArray();
    }
}


