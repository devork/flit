package com.flit.protoc.gen.server.undertow;

import com.flit.protoc.Parameter;
import com.flit.protoc.gen.Generator;
import com.flit.protoc.gen.server.ServiceGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.flit.protoc.Parameter.PARAM_CONTEXT;

public class UndertowGenerator implements Generator {
  @Override public List<PluginProtos.CodeGeneratorResponse.File> generate(PluginProtos.CodeGeneratorRequest request, Map<String, Parameter> params) {
    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();

    TypeMapper mapper = new TypeMapper();

    request.getProtoFileList().forEach(proto -> {

      mapper.add(proto);

      // Provide handlers for each service entry
      proto.getServiceList().forEach(s -> {

        String context = null;

        if (params.containsKey(PARAM_CONTEXT)) {
          context = params.get(PARAM_CONTEXT).getValue();
        }

        ServiceGenerator sgen = new ServiceGenerator(proto, s, mapper);
        RpcGenerator rgen = new RpcGenerator(proto, s, context, mapper);

        rgen.writeProlog();
        rgen.writePackage();
        rgen.writeImports();
        rgen.open(s);

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
