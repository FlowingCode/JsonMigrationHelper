package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonObject;

public class LegacyClientCallablePrivate_JsonObject__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonObject arg) {
        trace();
    }
}
