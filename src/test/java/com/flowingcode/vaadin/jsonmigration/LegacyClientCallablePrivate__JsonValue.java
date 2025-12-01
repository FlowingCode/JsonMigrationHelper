package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonValue;

public class LegacyClientCallablePrivate__JsonValue extends BaseClientCallable {

    @LegacyClientCallable
    private JsonValue test() {
        trace();
        return Json.createObject();
    }
}
