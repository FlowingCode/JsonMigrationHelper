package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

import elemental.json.Json;
import elemental.json.JsonNull;

public class ClientCallable__JsonNull extends BaseClientCallable {

    @ClientCallable
    public JsonNull test() {
        trace();
        return Json.createNull();
    }
}


