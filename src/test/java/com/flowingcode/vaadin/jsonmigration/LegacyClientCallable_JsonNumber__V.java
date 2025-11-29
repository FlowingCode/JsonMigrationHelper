package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonNumber;

public class LegacyClientCallable_JsonNumber__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonNumber arg) {
        trace();
    }
}
