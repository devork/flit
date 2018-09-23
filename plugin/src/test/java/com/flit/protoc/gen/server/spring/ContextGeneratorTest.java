package com.flit.protoc.gen.server.spring;

import com.flit.protoc.Plugin;
import com.flit.protoc.gen.BaseGeneratorTest;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests the generation of a service that has core definition imported from another file
 */
public class ContextGeneratorTest extends BaseGeneratorTest {

  @Test public void test_GenerateWithMissingRoot() throws Exception {
    test_Route("context.missing.spring.json", "/twirp/com.example.context.NullService/SayNull");
  }

  @Test public void test_GenerateWithEmptyRoot() throws Exception {
    test_Route("context.empty.spring.json", "/twirp/com.example.context.NullService/SayNull");
  }

  @Test public void test_GenerateWithSlashOnlyRoot() throws Exception {
    test_Route("context.slash.spring.json", "/com.example.context.NullService/SayNull");
  }

  @Test public void test_GenerateWithSlashRoot() throws Exception {
    test_Route("context.root.spring.json", "/root/com.example.context.NullService/SayNull");
  }

  @Test public void test_GenerateWithNameRoot() throws Exception {
    test_Route("context.name.spring.json", "/fibble/com.example.context.NullService/SayNull");
  }

  private void test_Route(String file, String route) throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson(file);

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());

    Map<String, PluginProtos.CodeGeneratorResponse.File> files = response.getFileList()
      .stream()
      .collect(Collectors.toMap(PluginProtos.CodeGeneratorResponse.File::getName, Function.identity()));

    assertTrue(files.containsKey("com/example/context/rpc/RpcNullService.java"));
    assertTrue(files.containsKey("com/example/context/rpc/RpcNullServiceController.java"));

    assertTrue(files.get("com/example/context/rpc/RpcNullServiceController.java")
      .getContent()
      .contains(String.format("@PostMapping(\"%s\")", route)));
  }

}
