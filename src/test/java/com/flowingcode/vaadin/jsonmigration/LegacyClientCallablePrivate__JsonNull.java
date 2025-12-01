package com.flowingcode.vaadin.jsonmigration;

import elemental.json.Json;
import elemental.json.JsonNull;

public class LegacyClientCallablePrivate__JsonNull extends BaseClientCallable {

    @LegacyClientCallable
    private JsonNull test() {
        trace();
        return Json.createNull();
    }
}
