package com.flit.protoc.gen.client;

import com.flit.protoc.Parameter;
import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.Generator;
import com.flit.protoc.gen.server.ServiceGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flit.protoc.Parameter.PARAM_CONTEXT;

public class BaseClientGenerator implements Generator {
    @Override public List<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request, Map<String, Parameter> params) {
        List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();
        String context = getContext(params);
        TypeMapper mapper = new TypeMapper(request.getProtoFileList());
        request.getProtoFileList().forEach(proto -> {
            proto.getServiceList().forEach(s -> {
                files.addAll(new ServiceGenerator(proto, s, mapper).getFiles());
                files.addAll(getRpcGenerator(proto, s, context, mapper).getFiles());
            });
        });
        return files;
    }

    private static String getContext(Map<String, Parameter> params) {
        if (params.containsKey(PARAM_CONTEXT)) {
            return params.get(PARAM_CONTEXT).getValue();
        }
        return null;
    }

    protected abstract BaseGenerator getRpcGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper);
}
