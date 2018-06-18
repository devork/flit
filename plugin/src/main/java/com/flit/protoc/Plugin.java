package com.flit.protoc;

import com.flit.protoc.gen.Generator;
import com.flit.protoc.gen.GeneratorException;
import com.flit.protoc.gen.server.spring.SpringGenerator;
import com.flit.protoc.gen.server.undertow.UndertowGenerator;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Map;

import static com.flit.protoc.Parameter.PARAM_TARGET;
import static com.flit.protoc.Parameter.PARAM_TYPE;

public class Plugin {

    private final PluginProtos.CodeGeneratorRequest request;

    public Plugin(PluginProtos.CodeGeneratorRequest request) {
        this.request = request;
    }

    public PluginProtos.CodeGeneratorResponse process() {
        if (!request.hasParameter()) {
            return PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .setError("Usage: --flit_out=target=server,type=[spring|undertow]:<PATH>")
                .build();
        }

        Map<String, Parameter> params = Parameter.of(request.getParameter());

        try {
            PluginProtos.CodeGeneratorResponse.Builder builder = PluginProtos.CodeGeneratorResponse.newBuilder();

            resolveGenerator(params).generate(request, params).forEach(builder::addFile);

            return builder.build();
        } catch (GeneratorException e) {
            return PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .setError(e.getMessage())
                .build();
        }
    }

    private Generator resolveGenerator(Map<String, Parameter> params) {

        if (!params.containsKey(PARAM_TARGET)) {
            throw new GeneratorException("No argument specified for target");
        }

        if (!params.containsKey(PARAM_TYPE)) {
            throw new GeneratorException("No argument specified for type");
        }

        switch (params.get(PARAM_TARGET).getValue()) {
            case "server":
                switch (params.get(PARAM_TYPE).getValue()) {
                    case "boot":
                    case "spring":
                        return new SpringGenerator();
                    case "undertow":
                        return new UndertowGenerator();
                    default:
                        throw new GeneratorException("Unknown server type: " + params.get(PARAM_TYPE).getValue());
                }
            default:
                throw new GeneratorException("Unknown target type: " + params.get(PARAM_TARGET).getValue());
        }

    }
}
