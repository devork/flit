package com.flit.protoc.gen.server.spring;

import com.flit.protoc.gen.server.BaseGenerator;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Collections;
import java.util.List;

class RpcGenerator extends BaseGenerator {

    private final String filename;
    private final String context;

    RpcGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context) {
        super(proto, service);
        this.filename = javaPackage.replace(".", "/") + "/Rpc" + this.service.getName() + "Controller.java";

        if (context == null) {
            this.context = "/twirp";
        } else {
            context = context.trim();
            if (context.equals("")) {
                // empty route - i.e. top level "/"
                this.context = context;
            } else if (context.startsWith("/")) {
                this.context = context;
            } else {
                this.context = "/" + context;
            }
        }
    }

    void writeImports() {
        // add imports
        b.wn("import com.google.protobuf.util.JsonFormat;");
        b.n();
        b.wn("import org.springframework.beans.factory.annotation.Autowired;");
        b.wn("import org.springframework.web.bind.annotation.PostMapping;");
        b.wn("import org.springframework.web.bind.annotation.RestController;");
        b.n();
        b.wn("import javax.servlet.http.HttpServletRequest;");
        b.wn("import javax.servlet.http.HttpServletResponse;");
        b.wn("import java.io.InputStreamReader;");
        b.wn("import java.nio.charset.Charset;");
        b.n();
    }

    void open() {
        b.wn("@RestController");
        b.wn("public class Rpc", service.getName(), "Controller {");
        b.n();

        // add the service handler
        b.inc();
        b.iwn("@Autowired");
        b.iwn("private Rpc", service.getName(), "Service service;");
        b.n();
    }

    void close() {
        b.dec();
        b.wn("}");
    }

    void writeService(DescriptorProtos.ServiceDescriptorProto s) {

        // for each service:
        //  1) Create a new interface for clients to implement logic
        //  2) Create a handler implementation to receive the data


        s.getMethodList().forEach(m -> {

            String route = context + "/" + (proto.hasPackage() ? proto.getPackage() + "." : "") + s.getName() + "/" + m.getName();

            // the spring mapping
            b.iwn("@PostMapping(value=\"", route, "\")");

            // the method name
            b.iwn("public void handle", m.getName(), "(HttpServletRequest request, HttpServletResponse response) throws Exception {");
            b.inc();

            // bind the data
            b.iwn("boolean json = false;");
            b.iwn(clazz, ".", basename(m.getInputType()), " data;");
            b.iwn("if (request.getContentType().equals(\"application/protobuf\")) {");
            b.inc();
            b.iwn("data = ", clazz, ".", basename(m.getInputType()), ".parseFrom(request.getInputStream());");
            b.dec();
            b.iwn("} else if (request.getContentType().startsWith(\"application/json\")) {");
            b.inc();
            b.iwn("json = true;");
            b.iwn(clazz, ".", basename(m.getInputType()), ".Builder builder = ", clazz, ".", basename(m.getInputType()), ".newBuilder();");
            b.iwn("JsonFormat.parser().merge(new InputStreamReader(request.getInputStream(), Charset.forName(\"UTF-8\")), builder);");
            b.iwn("data = builder.build();");
            b.dec();
            b.iwn("} else {");
            b.inc();
            b.iwn("response.setStatus(415);");
            b.iwn("return;");
            b.dec();
            b.iwn("}");
            b.n();

            // route to the service
            b.iwn(clazz, ".", basename(m.getOutputType()), " retval = ", "service.handle", m.getName(), "(data);");
            b.iwn("response.setStatus(200);");

            b.iwn("if (json) {");
            b.inc();
            b.iwn("response.setContentType(\"application/json;charset=UTF-8\");");
            b.iwn("response.getOutputStream().write(JsonFormat.printer().omittingInsignificantWhitespace().print(retval).getBytes(Charset.forName(\"UTF-8\")));");
            b.iwn("return;");
            b.dec();
            b.iwn("}");
            b.n();
            b.iwn("response.setContentType(\"application/protobuf\");");
            b.iwn("retval.writeTo(response.getOutputStream());");
            b.dec();

            b.iwn("}");
            b.n();
        });


    }

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
        PluginProtos.CodeGeneratorResponse.File.Builder builder = PluginProtos.CodeGeneratorResponse.File.newBuilder();
        builder.setName(filename);
        builder.setContent(b.toString());

        return Collections.singletonList(builder.build());
    }
}
