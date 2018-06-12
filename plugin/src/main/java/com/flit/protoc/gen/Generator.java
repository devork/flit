package com.flit.protoc.gen;

import com.google.protobuf.compiler.PluginProtos;

import java.util.List;

/**
 * Defines the handler for the generator step
 */
public interface Generator {
    List<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request);
}
