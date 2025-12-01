package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonString;

public class LegacyClientCallablePrivate__JsonString extends BaseClientCallable {

    @LegacyClientCallable
    private JsonString test() {
        trace();
        return Json.create("");
    }
}
