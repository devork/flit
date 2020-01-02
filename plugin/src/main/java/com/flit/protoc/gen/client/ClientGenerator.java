package com.flit.protoc.gen.client;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.TypeMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;

public class ClientGenerator extends BaseGenerator {
    private final TypeSpec.Builder rpcInterface;

    public ClientGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
        super(proto, s, mapper);
        rpcInterface = TypeSpec.interfaceBuilder(ClassName.get(javaPackage, service.getName()) + "Client");
        rpcInterface.addModifiers(Modifier.PUBLIC);
        service.getMethodList().forEach(this::addDispatchMethod);
    }

    private void addDispatchMethod(DescriptorProtos.MethodDescriptorProto m) {
        rpcInterface.addMethod(MethodSpec.methodBuilder("dispatch" + m.getName())
                .addParameter(mapper.get(m.getInputType()), "in")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .returns(mapper.get(m.getOutputType()))
                .build());
    }

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
        return Collections.singletonList(toFile(getServiceInterface(), rpcInterface.build()));
    }
}
