package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonNumber;

public class LegacyClientCallablePrivate__JsonNumber extends BaseClientCallable {

    @LegacyClientCallable
    private JsonNumber test() {
        trace();
        return Json.create(0);
    }
}
