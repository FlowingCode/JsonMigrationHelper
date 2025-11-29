package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonNull;

public class LegacyClientCallable__JsonNull extends BaseClientCallable {

    @LegacyClientCallable
    public JsonNull test() {
        trace();
        return Json.createNull();
    }
}
