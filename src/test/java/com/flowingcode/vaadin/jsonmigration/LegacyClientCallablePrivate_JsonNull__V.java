package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonNull;

public class LegacyClientCallablePrivate_JsonNull__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonNull arg) {
        trace();
    }
}
