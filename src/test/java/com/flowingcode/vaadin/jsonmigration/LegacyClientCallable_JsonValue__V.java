package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonValue;

public class LegacyClientCallable_JsonValue__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonValue arg) {
        trace();
    }
}
