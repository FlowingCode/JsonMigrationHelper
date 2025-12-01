package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonArray;

public class LegacyClientCallablePrivate_JsonArray__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonArray arg) {
        trace();
    }
}
