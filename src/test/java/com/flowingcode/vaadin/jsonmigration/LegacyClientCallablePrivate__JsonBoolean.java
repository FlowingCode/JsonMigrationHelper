package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonBoolean;

public class LegacyClientCallablePrivate__JsonBoolean extends BaseClientCallable {

    @LegacyClientCallable
    private JsonBoolean test() {
        trace();
        return Json.create(true);
    }
}
