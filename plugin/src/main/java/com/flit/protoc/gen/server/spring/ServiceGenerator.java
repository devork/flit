package com.flit.protoc.gen.server.spring;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Collections;
import java.util.List;

class ServiceGenerator extends BaseGenerator {

    private final String filename;

    ServiceGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s) {
        super(proto, s);
        this.filename = javaPackage.replace(".", "/") + "/Rpc" + service.getName() + "Service.java";
    }

    void open() {
        b.wn("public interface Rpc", service.getName(), "Service {");
        b.n();
        b.inc();
    }

    void close() {
        b.dec();
        b.wn("}");
    }

    void writeService(DescriptorProtos.ServiceDescriptorProto s) {

        s.getMethodList().forEach(m -> {
            b.iwn(clazz, ".", basename(m.getOutputType()), " handle", m.getName(), "(", clazz, ".", basename(m.getInputType())," in);");
            b.n();
        });

    }

    @Override
    List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
        PluginProtos.CodeGeneratorResponse.File.Builder builder = PluginProtos.CodeGeneratorResponse.File.newBuilder();
        builder.setName(filename);
        builder.setContent(b.toString());

        return Collections.singletonList(builder.build());
    }
}
