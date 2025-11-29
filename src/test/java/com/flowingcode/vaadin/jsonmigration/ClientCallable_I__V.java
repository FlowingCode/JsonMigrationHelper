package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

public class ClientCallable_I__V extends BaseClientCallable {

    @ClientCallable
    public void test(int arg) {
        trace();
    }
}


