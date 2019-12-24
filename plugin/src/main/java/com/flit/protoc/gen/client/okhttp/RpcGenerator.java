package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.Types;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.*;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static javax.lang.model.element.Modifier.*;

public class RpcGenerator extends BaseGenerator {
    private final String context;
    private final TypeSpec.Builder rpcHandler;

    RpcGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
        super(proto, service, mapper);
        this.context = getContext(context);
        rpcHandler = TypeSpec.classBuilder(getClientName(service))
                .addModifiers(PUBLIC);
        addStaticFields();
        addInstanceFields();
        addConstructor();
        service.getMethodList().forEach(this::writeDispatchMethod);
    }

    private ClassName getClientName(DescriptorProtos.ServiceDescriptorProto service) {
        return ClassName.get(javaPackage, "Rpc" + service.getName() + "Client");
    }

    private void addStaticFields() {
        // Add service path prix
        rpcHandler.addField(FieldSpec.builder(Types.String, "SERVICE_PATH_PREFIX")
        .addModifiers(PUBLIC, STATIC, FINAL)
        .initializer("\"$L/$L$L\"", context, proto.hasPackage() ? proto.getPackage() + "." : "", service.getName())
        .build());
    }

    private void addInstanceFields() {
        rpcHandler.addField(FieldSpec.builder(String.class, "baseAddress").addModifiers(PRIVATE, FINAL).build());
        rpcHandler.addField(FieldSpec.builder(OkHttpClient.class, "client").build());
        rpcHandler.addField(FieldSpec.builder(Headers.class, "headers").build());
    }

    private void addConstructor() {
        rpcHandler.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(String.class, "address")
        .addParameter(OkHttpClient.class, "client")
        .addParameter(Map.class, "headers")
        .addStatement("this.baseAddress = address")
        .addStatement("this.client = client")
        .addStatement("this.headers = Headers.of(headers)")
        .build());
    }

    private void writeDispatchMethod(DescriptorProtos.MethodDescriptorProto m) {
        ClassName inputType = mapper.get(m.getInputType());
        ClassName outputType = mapper.get(m.getOutputType());
        rpcHandler.addMethod(
          MethodSpec.methodBuilder("dispatch" + m.getName())
          .addModifiers(PRIVATE)
          .addParameter(inputType, "in")
          .returns(outputType)
          .addException(Exception.class)
          .addStatement("RequestBody requestBody = RequestBody.create(in.toByteArray(), MediaType.get(\"application/protobuf\"));")
          .addStatement("Request.Builder builder = new Request.Builder()")
          .addStatement("builder.addHeader(\"Accept\", \"application/protobuf\")")
          .addStatement("builder.addHeader(\"Content-Type\", \"application/protobuf\")")
          .addStatement("builder.addHeader(\"Flit-Version\", \"v1.1.0\")")
          .addStatement("builder.headers(headers)")
          .addStatement("builder.url(HttpUrl.parse(baseAddress + SERVICE_PATH_PREFIX + \"$S\")", m.getName())
          .addStatement("builder.post(requestBody)")
          .addStatement("Request request = builder.build()")
          .addStatement("String responseString")
          .addStatement("InputStream responseStream")
          .beginControlFlow("try(Response response = client.newCall(request).execute())")
          .beginControlFlow("if(response.code() != 200)")
          .addStatement("responseString = response.body().toString()")
          .addStatement("throw FlitException.builder().withErrorCode(ErrorCode.INTERNAL).withMeta(\"message\", responseString).withMessage(\"RPC error\").build()")
          .nextControlFlow("else")
          .addStatement("responseStream = response.body().byteStream()")
          .addStatement("return $T.parseFrom(responseStream)", outputType)
          .endControlFlow()
          .endControlFlow()
          .build()
        );
    }

    @Override
    public List<PluginProtos.CodeGeneratorResponse.File> getFiles() {
        return Collections.singletonList(toFile(getClientName(service), rpcHandler.build()));
    }
}
