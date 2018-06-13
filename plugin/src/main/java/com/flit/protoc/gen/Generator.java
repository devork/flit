package com.flit.protoc.gen;

import com.flit.protoc.Parameter;
import com.google.protobuf.compiler.PluginProtos;

import java.util.List;
import java.util.Map;

/**
 * Defines the handler for the generator step
 */
public interface Generator {

    /**
     *
     * @param request
     * @param params
     * @return
     */
    List<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request, Map<String, Parameter> params);
}
