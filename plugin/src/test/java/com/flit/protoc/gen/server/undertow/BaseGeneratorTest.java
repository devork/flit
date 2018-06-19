package com.flit.protoc.gen.server.undertow;

import com.google.protobuf.compiler.PluginProtos;

import java.io.InputStream;

public abstract class BaseGeneratorTest {

    public PluginProtos.CodeGeneratorRequest load(String resource) throws Exception {
        try (InputStream is = this.getClass().getClassLoader().getResource(resource).openStream()) {
            return PluginProtos.CodeGeneratorRequest
                .newBuilder()
                .mergeFrom(is)
                .build();
        }
    }
}
