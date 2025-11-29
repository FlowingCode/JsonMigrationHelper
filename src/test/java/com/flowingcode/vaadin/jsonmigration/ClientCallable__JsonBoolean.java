package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonBoolean;

public class ClientCallable__JsonBoolean extends BaseClientCallable {

    @ClientCallable
    public JsonBoolean test() {
        trace();
        return Json.create(true);
    }
}


