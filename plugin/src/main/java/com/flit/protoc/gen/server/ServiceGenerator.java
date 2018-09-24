package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * Generates the `Rpc${SerivceName}` interface.
 *
 * Currently this is the same interface across both undertow and spring.
 */
public class ServiceGenerator extends BaseGenerator {

  private final TypeSpec.Builder rpcInterface;

  public ServiceGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
    super(proto, s, mapper);
    rpcInterface = TypeSpec.interfaceBuilder(ClassName.get(javaPackage, "Rpc" + service.getName()));
    rpcInterface.addModifiers(Modifier.PUBLIC);
    service.getMethodList().forEach(this::addHandleMethod);
  }

  private void addHandleMethod(DescriptorProtos.MethodDescriptorProto m) {
    rpcInterface.addMethod(MethodSpec.methodBuilder("handle" + m.getName())
      .addParameter(mapper.get(m.getInputType()), "in")
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .returns(mapper.get(m.getOutputType()))
      .build());
  }

  @Override public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
    return Collections.singletonList(toFile(getServiceInterface(), rpcInterface.build()));
  }
}
