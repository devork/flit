package com.flit.runtime.undertow;

import io.undertow.Undertow;

public class Main {

    public static void main(final String[] args) {
        Undertow server = Undertow.builder()
            .addHttpListener(8080, "0.0.0.0")//Undertow builder
            .setHandler(new FlitHandler.Builder().withNext(null).build())
            .build();
        server.start();
    }
}
