package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonBoolean;

public class LegacyClientCallable__JsonBoolean extends BaseClientCallable {

    @LegacyClientCallable
    public JsonBoolean test() {
        trace();
        return Json.create(true);
    }
}
