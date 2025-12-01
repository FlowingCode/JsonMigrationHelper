package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonObject;

public class LegacyClientCallablePrivate__JsonObject extends BaseClientCallable {

    @LegacyClientCallable
    private JsonObject test() {
        trace();
        return Json.createObject();
    }
}
