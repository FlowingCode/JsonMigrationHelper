package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonString;

public class LegacyClientCallable_JsonString__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonString arg) {
        trace();
    }
}
