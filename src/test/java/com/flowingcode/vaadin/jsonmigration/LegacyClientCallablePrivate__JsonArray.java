package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonArray;

public class LegacyClientCallablePrivate__JsonArray extends BaseClientCallable {

    @LegacyClientCallable
    private JsonArray test() {
        trace();
        return Json.createArray();
    }
}
