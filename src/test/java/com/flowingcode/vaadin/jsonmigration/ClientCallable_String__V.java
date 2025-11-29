package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

public class ClientCallable_String__V extends BaseClientCallable {

    @ClientCallable
    public void test(String arg) {
        trace();
    }
}


