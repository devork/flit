package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.Plugin;
import com.flit.protoc.gen.BaseGeneratorTest;
import com.google.protobuf.compiler.PluginProtos;
import org.approvaltests.Approvals;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the generation of a service that has core definition imported from another file
 */
public class StatusGeneratorTest extends BaseGeneratorTest {

  @Test public void test_Generate() throws Exception {
    PluginProtos.CodeGeneratorRequest request = loadJson("status.okhttp.json");

    Plugin plugin = new Plugin(request);
    PluginProtos.CodeGeneratorResponse response = plugin.process();

    assertNotNull(response);
    assertEquals(2, response.getFileCount());

    assertEquals(response.getFile(0).getName(), "com/example/helloworld/RpcStatus.java");
    assertEquals(response.getFile(1).getName(), "com/example/helloworld/RpcStatusClient.java");

    Approvals.verifyAll("", response.getFileList().stream().map(f -> f.getContent()).collect(toList()));
    response.getFileList().forEach(f -> assertParses(f));
  }

}
