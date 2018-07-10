package com.flit.protoc.gen.server.undertow;

import com.flit.protoc.Plugin;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the generation of a service that has core definition imported from another file
 */
public class StatusGeneratorTest extends BaseGeneratorTest {

    @Test
    public void test_Generate() throws Exception {
        PluginProtos.CodeGeneratorRequest request = load("status.undertow.bin");

        Plugin plugin = new Plugin(request);
        PluginProtos.CodeGeneratorResponse response = plugin.process();

        assertNotNull(response);
        assertEquals(2, response.getFileCount());

        Map<String, PluginProtos.CodeGeneratorResponse.File> files = response.getFileList().stream().collect(Collectors.toMap(
            PluginProtos.CodeGeneratorResponse.File::getName,
            Function.identity()
        ));

        assertTrue(files.containsKey("com/example/helloworld/RpcStatus.java"));
        assertTrue(files.containsKey("com/example/helloworld/RpcStatusHandler.java"));

        test_Service(files.get("com/example/helloworld/RpcStatus.java"));
        test_Handler(files.get("com/example/helloworld/RpcStatusHandler.java"));
    }

    private void test_Handler(PluginProtos.CodeGeneratorResponse.File file) throws Exception {
        CompilationUnit cu = JavaParser.parse(file.getContent());
        cu.getPackageDeclaration().get().getName().asString();
        assertEquals("com.example.helloworld", cu.getPackageDeclaration().get().getName().asString());
        assertEquals(1, cu.getTypes().size());

        assertTrue(cu.getType(0).isPublic());
        assertEquals("RpcStatusHandler", cu.getType(0).getNameAsString());
        assertEquals("HttpHandler", ((ClassOrInterfaceDeclaration)cu.getType(0)).getImplementedTypes(0).getNameAsString());

        Map<String, MethodDeclaration> methods = cu
            .findAll(MethodDeclaration.class)
            .stream()
            .collect(Collectors.toMap(MethodDeclaration::getNameAsString, Function.identity()));

        assertEquals(2, methods.size());
        assertTrue(methods.containsKey("handleRequest"));
        assertTrue(methods.containsKey("handleGetStatus"));

        MethodDeclaration handleRequest = methods.get("handleRequest");
        assertEquals(1, handleRequest.getParameters().size());

        assertEquals("HttpServerExchange", handleRequest.getParameterByName("exchange").get().getTypeAsString());
        assertEquals("void", handleRequest.getTypeAsString());
        assertEquals("Exception", handleRequest.getThrownException(0).asString());

        MethodDeclaration handleHello = methods.get("handleGetStatus");
        assertEquals(1, handleHello.getParameters().size());

        assertEquals("HttpServerExchange", handleHello.getParameterByName("exchange").get().getTypeAsString());
        assertEquals("void", handleHello.getTypeAsString());
        assertEquals("Exception", handleHello.getThrownException(0).asString());

    }

    private void test_Service(PluginProtos.CodeGeneratorResponse.File file) throws Exception {
        CompilationUnit cu = JavaParser.parse(file.getContent());
        cu.getPackageDeclaration().get().getName().asString();
        assertEquals("com.example.helloworld", cu.getPackageDeclaration().get().getName().asString());
        assertEquals(1, cu.getTypes().size());

        assertTrue(cu.getType(0).isPublic());
        assertEquals("RpcStatus", cu.getType(0).getNameAsString());

        Map<String, MethodDeclaration> methods = cu
            .findAll(MethodDeclaration.class)
            .stream()
            .collect(Collectors.toMap(MethodDeclaration::getNameAsString, Function.identity()));

        assertEquals(1, methods.size());
        assertTrue(methods.containsKey("handleGetStatus"));

        MethodDeclaration handleHello = methods.get("handleGetStatus");
        assertEquals(1, handleHello.getParameters().size());

        assertEquals("Core.Empty", handleHello.getParameterByName("in").get().getTypeAsString());
        assertEquals("StatusOuterClass.StatusResponse", handleHello.getTypeAsString());
    }


}