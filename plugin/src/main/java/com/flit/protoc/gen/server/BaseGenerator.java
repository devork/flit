package com.flit.protoc.gen.server;

import com.flit.protoc.gen.Buffer;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.time.Instant;
import java.util.List;

public abstract class BaseGenerator {
    protected Buffer b = new Buffer();

    protected DescriptorProtos.ServiceDescriptorProto service;
    protected String javaPackage;
    protected String clazz;

    protected DescriptorProtos.FileDescriptorProto proto;

    protected BaseGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s) {
        this.clazz = proto.getOptions().getJavaOuterClassname();

        if (this.clazz == null || this.clazz.isEmpty()) {

            char[] classname = proto.getName().substring(0, proto.getName().lastIndexOf('.')).toCharArray();
            StringBuilder sb = new StringBuilder();

            char previous = '_';
            for (char c : classname) {
                if (c == '_') {
                    previous = c;
                    continue;
                }

                if (previous == '_') {
                    sb.append(Character.toUpperCase(c));
                } else {
                    sb.append(c);
                }

                previous = c;
            }

            this.clazz = sb.toString();

            // check to see if there are any messages with this same class name as per java proto specs
            // note that we also check the services too as the protoc compiler does that as well
            proto.getMessageTypeList().forEach(m -> {
                if (m.getName().equals(this.clazz)) {
                    this.clazz += "OuterClass";
                }
            });

            proto.getServiceList().forEach(m -> {
                if (m.getName().equals(this.clazz)) {
                    this.clazz += "OuterClass";
                }
            });
        }

        this.javaPackage = proto.getOptions().getJavaPackage();
        this.proto = proto;
        this.service = s;
    }

    public void writeProlog() {
        b.wn("// -------------------------------------------------------------");
        b.wn("// Generated code from flit: Please do not modify");
        b.wn("// Created: ", Instant.now().toString());
        b.wn("// -------------------------------------------------------------");
        b.wn("\n");
    }

    public void writePackage() {
        b.wn("package ", javaPackage, ";");
        b.n();
    }

    public abstract List<PluginProtos.CodeGeneratorResponse.File> getFiles();

    public static String basename(String name) {
        return basename(name, "\\.");
    }

    public static String basename(String name, String sep) {
        String[] parts = name.split(sep);

        return parts[parts.length - 1];
    }


}
