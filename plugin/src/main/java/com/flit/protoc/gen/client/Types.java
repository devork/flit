package com.flit.protoc.gen.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.function.Function;

public class Types {
    public static final ClassName String = ClassName.get(String.class);
    public static final ClassName OkHttpClient = ClassName.bestGuess("okhttp3.OkHttpClient");
    public static final ClassName Exception = ClassName.get(java.lang.Exception.class);
    public static final ClassName RequestBody = ClassName.bestGuess("okhttp3.RequestBody");
    public static final ClassName RequestBuilder = ClassName.bestGuess("okhttp3.Request.Builder");
    public static final ClassName MediaType = ClassName.bestGuess("okhttp3.MediaType");
    public static final ClassName InputStream = ClassName.bestGuess("java.io.InputStream");
    public static final ClassName HttpUrl = ClassName.bestGuess("okhttp3.HttpUrl");
    public static final ClassName Request = ClassName.bestGuess("okhttp3.Request");
    public static final ClassName Response = ClassName.bestGuess("okhttp3.Response");
    public static final ParameterizedTypeName BeforeRequestFunction = ParameterizedTypeName.get(ClassName.bestGuess("java.util.function.Function"), ClassName.bestGuess("BeforeRequestPayload"), RequestBuilder);
}
