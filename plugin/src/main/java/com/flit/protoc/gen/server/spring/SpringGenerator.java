package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.Generator;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring specific generator that will output MVC style routes.
 */
public class SpringGenerator implements Generator {

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request) {

        List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();

        request.getProtoFileList().forEach(proto -> {

            // Provide handlers for each service entry
            proto.getServiceList().forEach(s -> {

                ServiceGenerator sgen = new ServiceGenerator(proto, s);
                RpcGenerator rgen = new RpcGenerator(proto, s);

                rgen.writeProlog();
                rgen.writePackage();
                rgen.writeImports();
                rgen.open();

                sgen.writeProlog();
                sgen.writePackage();
                sgen.open();

                sgen.writeService(s);
                rgen.writeService(s);

                sgen.close();
                rgen.close();

                files.addAll(sgen.getFiles());
                files.addAll(rgen.getFiles());
            });


        });

        return files;
    }
}
