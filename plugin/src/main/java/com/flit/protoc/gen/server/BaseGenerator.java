package com.flit.protoc.gen.server;

import com.flit.protoc.gen.Buffer;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.List;

import static com.flit.protoc.gen.server.TypeMapper.getClassname;

public abstract class BaseGenerator {

  protected final Buffer b = new Buffer();
  protected final DescriptorProtos.ServiceDescriptorProto service;
  protected final String javaPackage;
  protected final String clazz;
  protected final TypeMapper mapper;

  protected DescriptorProtos.FileDescriptorProto proto;

  protected BaseGenerator(DescriptorProtos.FileDescriptorProto proto, DescriptorProtos.ServiceDescriptorProto s, TypeMapper mapper) {
    this.clazz = getClassname(proto);
    this.javaPackage = proto.getOptions().getJavaPackage();
    this.proto = proto;
    this.service = s;
    this.mapper = mapper;
  }

  public void writeProlog() {
    b.wn("// -------------------------------------------------------------");
    b.wn("// Generated code from flit: Please do not modify");
    b.wn("// -------------------------------------------------------------");
    b.wn("\n");
  }

  public void writePackage() {
    b.wn("package ", javaPackage, ";");
    b.n();
  }

  public abstract List<PluginProtos.CodeGeneratorResponse.File> getFiles();

  public static String basename(String name) {
    return basename(name, "\\.");
  }

  public static String basename(String name, String sep) {
    String[] parts = name.split(sep);

    return parts[parts.length - 1];
  }

  protected String getFileName(String className) {
    return javaPackage.replace(".", "/") + "/" + className + ".java";
  }

  protected static String getContext(String context) {
    if (context == null) {
      return "/twirp";
    } else {
      context = context.trim();
      if (context.equals("") || context.equals("/")) {
        return ""; // empty route - i.e. top level "/"
      } else if (context.startsWith("/")) {
        return context;
      } else {
        return "/" + context;
      }
    }
  }

}
