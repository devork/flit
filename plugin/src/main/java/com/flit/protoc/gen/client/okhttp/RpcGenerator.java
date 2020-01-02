package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.TypeMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import com.squareup.javapoet.*;

import java.util.Collections;
import java.util.List;

import static com.flit.protoc.gen.server.Types.ErrorCode;
import static com.flit.protoc.gen.server.Types.FlitException;
import static javax.lang.model.element.Modifier.*;
import static com.flit.protoc.gen.client.Types.*;

public class RpcGenerator extends BaseGenerator {
    private final String context;
    private final TypeSpec.Builder rpcDispatcher;

    RpcGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto service, String context, TypeMapper mapper) {
        super(proto, service, mapper);
        this.context = getContext(context);
        rpcDispatcher = TypeSpec.classBuilder(getClientName(service))
                .addModifiers(PUBLIC);
        addRequestPayload();
        addStaticFields();
        addInstanceFields();
        addConstructor();
        service.getMethodList().forEach(this::writeDispatchMethod);
    }

    private ClassName getClientName(DescriptorProtos.ServiceDescriptorProto service) {
        return ClassName.get(javaPackage, "Rpc" + service.getName() + "Dispatcher");
    }

    // TODO
    // add setBeforeRequest
    // re-do dispatch method to call function

    private void addRequestPayload() {
        // Add inner class for request payload
        TypeSpec.Builder payloadBuilder = TypeSpec.classBuilder("BeforeRequestPayload").addModifiers(PUBLIC);
        payloadBuilder.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(RequestBuilder, "builder")
        .addParameter(HttpUrl, "url")
        .addStatement("this.builder = builder")
        .addStatement("this.url = url")
        .build());
        payloadBuilder.addField(FieldSpec.builder(RequestBuilder, "builder").addModifiers(PUBLIC).build());
        payloadBuilder.addField(FieldSpec.builder(HttpUrl, "url").addModifiers(PUBLIC).build());

        // Add to dispatcher
        rpcDispatcher.addType(payloadBuilder.build());
    }

    private void addStaticFields() {
        // Add service path prix
        rpcDispatcher.addField(FieldSpec.builder(String, "SERVICE_PATH_PREFIX")
        .addModifiers(PUBLIC, STATIC, FINAL)
        .initializer("\"$L/$L$L\"", context, proto.hasPackage() ? proto.getPackage() + "." : "", service.getName())
        .build());
    }

    private void addInstanceFields() {
        rpcDispatcher.addField(FieldSpec.builder(String, "baseAddress").addModifiers(PRIVATE, FINAL).build());
        rpcDispatcher.addField(FieldSpec.builder(OkHttpClient, "client").addModifiers(PRIVATE).build());
        rpcDispatcher.addField(FieldSpec.builder(BeforeRequestFunction, "beforeRequest").build());
    }

    private void addConstructor() {
        rpcDispatcher.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(String, "address")
        .addParameter(OkHttpClient, "client")
        .addStatement("this.baseAddress = address")
        .addStatement("this.client = client")
        .build());
    }

    private void writeDispatchMethod(DescriptorProtos.MethodDescriptorProto m) {
        ClassName inputType = mapper.get(m.getInputType());
        ClassName outputType = mapper.get(m.getOutputType());
        rpcDispatcher.addMethod(
          MethodSpec.methodBuilder("dispatch" + m.getName())
          .addModifiers(PUBLIC)
          .addParameter(inputType, "in")
          .returns(outputType)
          .addException(Exception)
          .addStatement("$T requestBody = $T.create(in.toByteArray(), $T.get(\"application/protobuf\"))", RequestBody, RequestBody, MediaType)
          .addStatement("$T builder = new $T()", RequestBuilder, RequestBuilder)
          .addStatement("builder.addHeader(\"Accept\", \"application/protobuf\")")
          .addStatement("builder.addHeader(\"Content-Type\", \"application/protobuf\")")
          .addStatement("builder.addHeader(\"Flit-Version\", \"v1.1.0\")")
          .addStatement("builder.headers(headers)")
          .addStatement("builder.url($T.parse(baseAddress + SERVICE_PATH_PREFIX + $S))", HttpUrl, m.getName())
          .addStatement("builder.post(requestBody)")
          .addStatement("$T request = builder.build()", Request)
          .addStatement("String responseString")
          .addStatement("$T responseStream", InputStream)
          .beginControlFlow("try($T response = client.newCall(request).execute())", Response)
          .beginControlFlow("if(response.code() != 200)")
          .addStatement("responseString = response.body().toString()")
          .addStatement("throw $T.builder().withErrorCode($T.INTERNAL).withMeta(\"message\", responseString).withMessage(\"RPC error\").build()", FlitException, ErrorCode)
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
        return Collections.singletonList(toFile(getClientName(service), rpcDispatcher.build()));
    }
}
