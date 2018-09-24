package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.BaseServerGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.ServiceDescriptorProto;

/**
 * Spring specific generator that will output MVC style routes.
 */
public class SpringGenerator extends BaseServerGenerator {

  @Override protected BaseGenerator getRpcGenerator(
    FileDescriptorProto proto, ServiceDescriptorProto service, String context, TypeMapper mapper) {
    return new RpcGenerator(proto, service, context, mapper);
  }

}
