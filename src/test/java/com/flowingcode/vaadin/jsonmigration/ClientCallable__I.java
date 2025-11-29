package com.flowingcode.vaadin.jsonmigration;


import com.vaadin.flow.component.ClientCallable;

public class ClientCallable__I extends BaseClientCallable {

    @ClientCallable
    public int test() {
        trace();
        return 0;
    }
}


