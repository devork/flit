package com.flit.protoc;

import com.flit.protoc.gen.server.spring.SpringGenerator;
import com.google.protobuf.compiler.PluginProtos;
import com.flit.protoc.gen.Generator;
import com.flit.protoc.gen.GeneratorException;

import java.util.Map;

public class Main {

    private static final String PARAM_TARGET = "target";
    private static final String PARAM_CLIENT = "client";
    private static final String PARAM_TYPE = "type";

    public static void main(String[] args) throws Exception {

        PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.newBuilder().mergeFrom(System.in).build();

        if (!request.hasParameter()) {
            PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .setError("Usage: --flit_out=target=server,type=[spring]:<PATH> or --flit_out=target=client,type=[js]:<PATH>")
                .build()
                .writeTo(System.out);

            return;
        }


        try {
            PluginProtos.CodeGeneratorResponse.Builder builder = PluginProtos.CodeGeneratorResponse.newBuilder();

            resolveGenerator(request).generate(request).forEach(builder::addFile);

            builder.build().writeTo(System.out);
        } catch (GeneratorException e) {
            PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .setError(e.getMessage())
                .build()
                .writeTo(System.out);
        }
    }

    private static Generator resolveGenerator(PluginProtos.CodeGeneratorRequest request) {
        Map<String, Parameter> params = Parameter.of(request.getParameter());

        if (!params.containsKey(PARAM_TARGET)) {
            throw new GeneratorException("No argument specified for target");
        }

        switch (params.get(PARAM_TARGET).getValue()) {
            case "server":
                switch (params.get(PARAM_TYPE).getValue()) {
                    case "boot":
                    case "spring":
                        return new SpringGenerator();
                    default:
                        throw new GeneratorException("Unknown type: " + params.get(PARAM_TYPE).getValue());
                }
            default:
                throw new GeneratorException("Unknown target type: " + params.get(PARAM_TARGET).getValue());
        }

    }
}
