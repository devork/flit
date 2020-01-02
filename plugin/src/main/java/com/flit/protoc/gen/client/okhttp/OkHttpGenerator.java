package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.client.BaseClientGenerator;
import com.flit.protoc.gen.TypeMapper;
import com.google.protobuf.DescriptorProtos;

public class OkHttpGenerator extends BaseClientGenerator {
    @Override
    protected BaseGenerator getRpcGenerator(
            DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
        return new RpcGenerator(proto, service, context, mapper);
    }
}
