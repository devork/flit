package com.flit.protoc.gen.client.okhttp;

import com.flit.protoc.gen.BaseGenerator;
import com.flit.protoc.gen.server.TypeMapper;
import com.flit.protoc.gen.server.Types;
import com.google.protobuf.DescriptorProtos;
import com.squareup.javapoet.*;
import okhttp3.OkHttpClient;

import static javax.lang.model.element.Modifier.*;

public class RpcGenerator extends BaseGenerator {
    private final String context;
    private final TypeSpec.Builder rpcHandler;

    // TODO - both protobuf and json clients need to be generated?
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
    }

    private void addConstructor() {
        rpcHandler.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(String.class, "address")
        .addParameter(OkHttpClient.class, "client")
        .addStatement("this.baseAddress = address")
        .addStatement("this.client = client")
        .build());
    }

//    RequestBody requestBody = RequestBody.create(in.toByteArray(), MediaType.get("application/protobuf"));
//    Request request = new Request.Builder()
//            .addHeader("Accept", "application/protobuf")
//            .addHeader("Content-Type", "application/protobuf")
//            .url(HttpUrl.parse(baseAddress + SERVICE_PATH_PREFIX + "/Healthz"))
//            .post(requestBody)
//            .build();
//
//    String responseString;
//    InputStream responseStream;
//        try(Response response = client.newCall(request).execute()) {
//        if(response.code() != 200) {
//            responseString = response.body().toString();
//            throw FlitException.builder()
//                    .withErrorCode(ErrorCode.INTERNAL)
//                    .withMeta("code", response.code())
//                    .withMeta("message", responseString)
//                    .withMessage("RPC error")
//                    .build();
//        } else {
//            responseStream = response.body().byteStream();
//            return Query.HealthzResponse.parseFrom(responseStream);
//        }
//    }
    private void writeDispatchMethod(DescriptorProtos.MethodDescriptorProto m) {
        ClassName inputType = mapper.get(m.getInputType());
        ClassName outputType = mapper.get(m.getOutputType());
        rpcHandler.addMethod(
          MethodSpec.methodBuilder("dispatch" + m.getName())
          .addModifiers(PRIVATE)
          .addParameter(inputType, "in")
          .returns(outputType)
          .addException(Exception.class)
          // TODO - look into control flow builders!
          .addStatement("RequestBody requestBody = RequestBody.create(in.toByteArray(), MediaType.get(\"application/protobuf\"));")
          .addStatement("Request request = new Request.Builder()")
          .addStatement(".addHeader(\"Accept\", \"application/protobuf\")")
          .addStatement(".addHeader(\"Content-Type\", \"application/protobuf\")")
          .addStatement(".addHeader(\"Twirp-Version\", \"v1.1.0\")")
          .addStatement(".url(HttpUrl.parse(baseAddress + SERVICE_PATH_PREFIX + \"/$S\")", m.getName())
          .addStatement(".post(requestBody).build()")
          .addStatement("String responseString")
          .addStatement("InputStream responseStream")
          .addStatement("try(Response response = client.newCall(request).execute()) {")
          .addStatement("if(response.code() != 200) {")
          .addStatement("responseString = response.body().toString()")
          .addStatement("throw FlitException.builder().withErrorCode(ErrorCode.INTERNAL).withMeta(\"message\", responseString).withMessage(\"RPC error\").build()")
          .addStatement("} else {")
          .addStatement("responseStream = response.body().byteStream()")
          .addStatement("return $T.parseFrom(responseStream)", m.getOutputType())
          .addStatement("}")
          .build()
        );
    }
}
