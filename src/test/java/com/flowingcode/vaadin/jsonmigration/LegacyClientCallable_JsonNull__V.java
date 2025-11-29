package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonNull;

public class LegacyClientCallable_JsonNull__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonNull arg) {
        trace();
    }
}
