package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonValue;

public class LegacyClientCallable__JsonValue extends BaseClientCallable {

    @LegacyClientCallable
    public JsonValue test() {
        trace();
        return Json.createObject();
    }
}
