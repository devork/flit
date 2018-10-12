package com.flit.protoc.gen;

import com.flit.protoc.Parameter;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;

import java.util.List;
import java.util.Map;

/**
 * Defines the handler for the generator step
 */
public interface Generator {

  /**
   * This is the main entry point to code generation: the implementation is decided in the {@link com.flit.protoc.Plugin}
   * class from the given parameters.
   *
   * @param request   The inbound protoc request
   * @param params    The plugin parameters
   *
   * @return The list of files to be added to the output.
   */
  List<CodeGeneratorResponse.File> generate(CodeGeneratorRequest request, Map<String, Parameter> params);
}
