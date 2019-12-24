package com.example.helloworld;

import java.lang.Exception;
import java.lang.String;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

public class RpcHelloWorldClient {
    public static final String SERVICE_PATH_PREFIX = "/twirp/com.example.helloworld.HelloWorld";

    private final String baseAddress;

    OkHttpClient client;

    Headers headers;

    public RpcHelloWorldClient(String address, OkHttpClient client, Map headers) {
        this.baseAddress = address;
        this.client = client;
        this.headers = Headers.of(headers);
    }

    private Helloworld.HelloResp dispatchHello(Helloworld.HelloReq in) throws Exception {
        RequestBody requestBody = RequestBody.create(in.toByteArray(), MediaType.get("application/protobuf"));;
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Accept", "application/protobuf");
        builder.addHeader("Content-Type", "application/protobuf");
        builder.addHeader("Flit-Version", "v1.1.0");
        builder.headers(headers);
        builder.url(HttpUrl.parse(baseAddress + SERVICE_PATH_PREFIX + ""Hello"");
        builder.post(requestBody);
        Request request = builder.build();
        String responseString;
        InputStream responseStream;
        try(Response response = client.newCall(request).execute()) {
            if(response.code() != 200) {
                responseString = response.body().toString();
                throw FlitException.builder().withErrorCode(ErrorCode.INTERNAL).withMeta("message", responseString).withMessage("RPC error").build();
            } else {
                responseStream = response.body().byteStream();
                return Helloworld.HelloResp.parseFrom(responseStream);
            }
        }
    }

    private Helloworld.HelloResp dispatchHelloAgain(Helloworld.HelloReq in) throws Exception {
        RequestBody requestBody = RequestBody.create(in.toByteArray(), MediaType.get("application/protobuf"));;
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Accept", "application/protobuf");
        builder.addHeader("Content-Type", "application/protobuf");
        builder.addHeader("Flit-Version", "v1.1.0");
        builder.headers(headers);
        builder.url(HttpUrl.parse(baseAddress + SERVICE_PATH_PREFIX + ""HelloAgain"");
        builder.post(requestBody);
        Request request = builder.build();
        String responseString;
        InputStream responseStream;
        try(Response response = client.newCall(request).execute()) {
            if(response.code() != 200) {
                responseString = response.body().toString();
                throw FlitException.builder().withErrorCode(ErrorCode.INTERNAL).withMeta("message", responseString).withMessage("RPC error").build();
            } else {
                responseStream = response.body().byteStream();
                return Helloworld.HelloResp.parseFrom(responseStream);
            }
        }
    }
}
