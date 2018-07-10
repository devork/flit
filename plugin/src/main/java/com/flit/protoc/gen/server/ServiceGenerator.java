package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Collections;
import java.util.List;

public class ServiceGenerator extends BaseGenerator {

    private final String filename;

    public ServiceGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
        super(proto, s, mapper);
        this.filename = javaPackage.replace(".", "/") + "/Rpc" + service.getName() + ".java";
    }

    public void open() {
        b.wn("public interface Rpc", service.getName(), " {");
        b.n();
        b.inc();
    }

    public void close() {
        b.dec();
        b.wn("}");
    }

    public void writeService(DescriptorProtos.ServiceDescriptorProto s) {

        s.getMethodList().forEach(m -> {
            b.iwn(mapper.get(m.getOutputType()), " handle", m.getName(), "(", mapper.get(m.getInputType())," in);");
            b.n();
        });

    }

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
        PluginProtos.CodeGeneratorResponse.File.Builder builder = PluginProtos.CodeGeneratorResponse.File.newBuilder();
        builder.setName(filename);
        builder.setContent(b.toString());

        return Collections.singletonList(builder.build());
    }
}
