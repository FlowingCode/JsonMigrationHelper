package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonString;

public class LegacyClientCallable__JsonString extends BaseClientCallable {

    @LegacyClientCallable
    public JsonString test() {
        trace();
        return Json.create("");
    }
}
