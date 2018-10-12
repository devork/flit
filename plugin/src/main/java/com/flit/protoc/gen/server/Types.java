package com.flit.protoc.gen.server;

import com.squareup.javapoet.ClassName;

public class Types {
  public static final ClassName Override = ClassName.get(java.lang.Override.class);
  public static final ClassName ErrorCode = ClassName.bestGuess("com.flit.runtime.ErrorCode");
  public static final ClassName String = ClassName.get(String.class);
  public static final ClassName Exception = ClassName.get(java.lang.Exception.class);
  public static final ClassName InputStreamReader = ClassName.get(java.io.InputStreamReader.class);
  public static final ClassName StandardCharsets = ClassName.get(java.nio.charset.StandardCharsets.class);
  public static final ClassName LoggerFactory = ClassName.bestGuess("org.slf4j.LoggerFactory");
  public static final ClassName Logger = ClassName.bestGuess("org.slf4j.Logger");
  public static final ClassName JsonFormat = ClassName.get(com.google.protobuf.util.JsonFormat.class);
  public static final ClassName ErrorWriter = ClassName.bestGuess("com.flit.runtime.undertow.ErrorWriter");
  public static final ClassName HttpServerExchange = ClassName.bestGuess("io.undertow.server.HttpServerExchange");
  public static final ClassName Headers = ClassName.bestGuess("io.undertow.util.Headers");
  public static final ClassName FlitHandler = ClassName.bestGuess("com.flit.runtime.undertow.FlitHandler");
  public static final ClassName FlitException = ClassName.bestGuess("com.flit.runtime.FlitException");
}
