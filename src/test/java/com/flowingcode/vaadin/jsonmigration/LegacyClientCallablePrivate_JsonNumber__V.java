package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonNumber;

public class LegacyClientCallablePrivate_JsonNumber__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonNumber arg) {
        trace();
    }
}
