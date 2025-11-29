package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonArray;

public class LegacyClientCallable__JsonArray extends BaseClientCallable {

    @LegacyClientCallable
    public JsonArray test() {
        trace();
        return Json.createArray();
    }
}
