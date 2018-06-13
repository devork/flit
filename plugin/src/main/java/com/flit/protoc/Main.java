package com.flit.protoc;

import com.flit.protoc.gen.server.spring.SpringGenerator;
import com.google.protobuf.compiler.PluginProtos;
import com.flit.protoc.gen.Generator;
import com.flit.protoc.gen.GeneratorException;

import java.util.Map;

import static com.flit.protoc.Parameter.PARAM_TARGET;
import static com.flit.protoc.Parameter.PARAM_TYPE;

public class Main {

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

        Map<String, Parameter> params = Parameter.of(request.getParameter());

        try {
            PluginProtos.CodeGeneratorResponse.Builder builder = PluginProtos.CodeGeneratorResponse.newBuilder();

            resolveGenerator(params).generate(request, params).forEach(builder::addFile);

            builder.build().writeTo(System.out);
        } catch (GeneratorException e) {
            PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .setError(e.getMessage())
                .build()
                .writeTo(System.out);
        }
    }

    private static Generator resolveGenerator(Map<String, Parameter> params) {

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
