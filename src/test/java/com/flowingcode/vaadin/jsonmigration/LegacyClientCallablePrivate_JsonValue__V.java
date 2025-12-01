package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonValue;

public class LegacyClientCallablePrivate_JsonValue__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonValue arg) {
        trace();
    }
}
