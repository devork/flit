package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.client.BaseClientGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.undertow.RpcGenerator;
import com.google.protobuf.DescriptorProtos;

public class OkhttpGenerator extends BaseClientGenerator {
    @Override protected BaseGenerator getRpcGenerator(
            DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
        return new RpcGenerator(proto, service, context, mapper);
    }
}
