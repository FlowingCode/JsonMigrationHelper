package com.flowingcode.vaadin.jsonmigration;

import elemental.json.JsonString;

public class LegacyClientCallablePrivate_JsonString__V extends BaseClientCallable {

    @LegacyClientCallable
    private void test(JsonString arg) {
        trace();
    }
}
