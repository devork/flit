package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.Buffer;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.time.Instant;
import java.util.List;

abstract class BaseGenerator {
    Buffer b = new Buffer();

    DescriptorProtos.ServiceDescriptorProto service;
    String javaPackage;
    String clazz;

    DescriptorProtos.FileDescriptorProto proto;

    BaseGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s) {
        String baseClassName = proto.getOptions().getJavaOuterClassname();

        if (baseClassName == null || baseClassName.isEmpty()) {
            baseClassName = proto.getName().substring(0, proto.getName().lastIndexOf('.'));
        }

        char[] className = baseClassName.toCharArray();
        if (!Character.isUpperCase(className[0])) {
            className[0] = Character.toUpperCase(className[0]);
        }

        this.clazz = new String(className);
        this.javaPackage = proto.getOptions().getJavaPackage();
        this.proto = proto;
        this.service = s;
    }

    void writeProlog() {
        b.wn("// -------------------------------------------------------------");
        b.wn("// Generated code from flit: Please do not modify");
        b.wn("// Created: ", Instant.now().toString());
        b.wn("// -------------------------------------------------------------");
        b.wn("\n");
    }

    void writePackage() {
        b.wn("package ", javaPackage, ";");
        b.n();
    }

    abstract List<PluginProtos.CodeGeneratorResponse.File> getFiles();

    static String basename(String name) {
        return basename(name, "\\.");
    }

    static String basename(String name, String sep) {
        String[] parts = name.split(sep);

        return parts[parts.length - 1];
    }


}
