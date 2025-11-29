package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonNumber;

public class LegacyClientCallable__JsonNumber extends BaseClientCallable {

    @LegacyClientCallable
    public JsonNumber test() {
        trace();
        return Json.create(0);
    }
}
