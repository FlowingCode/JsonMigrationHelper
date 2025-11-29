package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonArray;

public class LegacyClientCallable_JsonArray__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonArray arg) {
        trace();
    }
}
