package com.flit.protoc;

import com.google.protobuf.compiler.PluginProtos;

/**
 * Main entry point to the plugin which handles the parsing of the input from the main protoc process
 * and hands off to a {@link Plugin} instance.
 */
public class Main {

  public static void main(String[] args) throws Exception {
    Plugin plugin = new Plugin(PluginProtos.CodeGeneratorRequest.newBuilder().mergeFrom(System.in).build());
    plugin.process().writeTo(System.out);
  }

}
