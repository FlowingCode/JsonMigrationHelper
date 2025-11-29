package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonObject;

public class LegacyClientCallable__JsonObject extends BaseClientCallable {

    @LegacyClientCallable
    public JsonObject test() {
        trace();
        return Json.createObject();
    }
}
