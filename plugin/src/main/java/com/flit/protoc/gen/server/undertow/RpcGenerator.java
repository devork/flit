package com.flit.protoc.gen.server.undertow;

import com.flit.protoc.gen.server.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Collections;
import java.util.List;

class RpcGenerator extends BaseGenerator {

    private final String filename;
    private final String context;

    RpcGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
        super(proto, service, mapper);
        this.filename = javaPackage.replace(".", "/") + "/Rpc" + this.service.getName() + "Handler.java";

        if (context == null) {
            this.context = "/twirp";
        } else {
            context = context.trim();
            if (context.equals("") || context.equals("/")) {
                // empty route - i.e. top level "/"
                this.context = "";
            } else if (context.startsWith("/")) {
                this.context = context;
            } else {
                this.context = "/" + context;
            }
        }
    }

    void writeImports() {
        // add imports
        b.wn("import com.flit.runtime.undertow.ErrorWriter;");
        b.wn("import com.google.protobuf.util.JsonFormat;");
        b.n();

        b.wn("import com.flit.runtime.ErrorCode;");
        b.wn("import com.flit.runtime.FlitException;");
        b.wn("import io.undertow.server.HttpHandler;");
        b.wn("import io.undertow.server.HttpServerExchange;");
        b.wn("import io.undertow.util.Headers;");
        b.wn("import org.slf4j.Logger;");
        b.wn("import org.slf4j.LoggerFactory;");

        b.n();
        b.wn("import java.io.InputStreamReader;");
        b.wn("import java.nio.charset.Charset;");
        b.n();
        b.wn("import static com.flit.runtime.undertow.FlitHandler.KEY_METHOD;");
        b.n();
    }

    void open(DescriptorProtos.ServiceDescriptorProto s) {
        b.wn("public class Rpc", service.getName(), "Handler implements HttpHandler {");
        b.n();

        // add a logger
        b.inc();
        b.iwn("private static final Logger LOGGER = LoggerFactory.getLogger(Rpc", service.getName(), "Handler.class);");

        // add the static route name
        b.iwn("public static final String ROUTE = \"", context, "/",(proto.hasPackage() ? proto.getPackage() + "." : ""),  s.getName(), "\";");
        b.n();

        // add the service handler
        b.iwn("private final Rpc", service.getName(), "Service service;");
        b.iwn("private final ErrorWriter errorWriter;");
        b.n();

        // add the constructor for the service
        b.iwn("public Rpc", service.getName(), "Handler(Rpc", service.getName() + "Service service) {");
        b.inc();
        b.iwn("this.service = service;");
        b.iwn("this.errorWriter = new ErrorWriter();");
        b.dec();
        b.iwn("}");
        b.n();

    }

    void close() {
        b.dec();
        b.wn("}");
    }

    void writeService(DescriptorProtos.ServiceDescriptorProto s) {

        // write the routing table
        b.iwn("@Override");
        b.iwn("public void handleRequest(HttpServerExchange exchange) throws Exception {");
        b.inc();
        b.iwn("if (exchange.isInIoThread()) {");
        b.inc();
        b.iwn("exchange.dispatch(this);");
        b.iwn("return;");
        b.dec();
        b.iwn("}");
        b.n();
        b.iwn("exchange.startBlocking();");
        b.n();
        b.iwn("String method = exchange.getAttachment(KEY_METHOD);");
        b.n();
        b.iwn("try {");
        b.inc();
        b.iwn("switch(method) {");
        b.inc();
        s.getMethodList().forEach(m -> b.iwn("case \"", m.getName(), "\": handle", m.getName(), "(exchange); break;"));
        b.iwn("default:");
        b.inc();
        b.iwn("throw FlitException.builder().withErrorCode(ErrorCode.BAD_ROUTE).withMessage(\"No such route\").build();");
        b.dec();

        b.dec();
        b.iwn("}");
        b.dec();
        b.iwn("} catch (FlitException e) {");
        b.inc();
        b.iwn("errorWriter.write(e, exchange);");
        b.dec();
        b.iwn("} catch (Exception e) {");
        b.inc();
        b.iwn("LOGGER.error(\"Exception caught at handler: error = {}\", e.getMessage(), e);");
        b.iwn("errorWriter.write(e, exchange);");
        b.dec();
        b.iwn("}");

        b.dec();
        b.iwn("}");
        b.n();

        s.getMethodList().forEach(m -> {

            // the method name
            b.iwn("private void handle", m.getName(), "(HttpServerExchange exchange) throws Exception {");
            b.inc();

            // bind the data
            b.iwn("boolean json = false;");
            b.iwn(mapper.get(m.getInputType()), " data;");
            b.iwn("if (exchange.getRequestHeaders().get(Headers.CONTENT_TYPE).getFirst().equals(\"application/protobuf\")) {");
            b.inc();
            b.iwn("data = ", mapper.get(m.getInputType()), ".parseFrom(exchange.getInputStream());");
            b.dec();
            b.iwn("} else if (exchange.getRequestHeaders().get(Headers.CONTENT_TYPE).getFirst().startsWith(\"application/json\")) {");
            b.inc();
            b.iwn("json = true;");
            b.iwn(mapper.get(m.getInputType()), ".Builder builder = ", mapper.get(m.getInputType()), ".newBuilder();");
            b.iwn("JsonFormat.parser().merge(new InputStreamReader(exchange.getInputStream(), Charset.forName(\"UTF-8\")), builder);");
            b.iwn("data = builder.build();");
            b.dec();
            b.iwn("} else {");
            b.inc();
            b.iwn("exchange.setStatusCode(415);");
            b.iwn("return;");
            b.dec();
            b.iwn("}");
            b.n();

            // route to the service
            b.iwn(mapper.get(m.getOutputType()), " retval = ", "service.handle", m.getName(), "(data);");
            b.iwn("exchange.setStatusCode(200);");

            b.iwn("if (json) {");
            b.inc();
            b.iwn("exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, \"application/json;charset=UTF-8\");");
            b.iwn("exchange.getResponseSender().send(JsonFormat.printer().omittingInsignificantWhitespace().print(retval));");
            b.iwn("return;");
            b.dec();
            b.iwn("}");
            b.n();
            b.iwn("exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, \"application/protobuf\");");
            b.iwn("retval.writeTo(exchange.getOutputStream());");
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
