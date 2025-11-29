package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonObject;

public class LegacyClientCallable_JsonObject__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonObject arg) {
        trace();
    }
}
