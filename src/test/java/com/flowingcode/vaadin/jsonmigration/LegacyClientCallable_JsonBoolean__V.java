package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonBoolean;

public class LegacyClientCallable_JsonBoolean__V extends BaseClientCallable {

    @LegacyClientCallable
    public void test(JsonBoolean arg) {
        trace();
    }
}
