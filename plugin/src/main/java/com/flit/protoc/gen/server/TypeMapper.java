package com.flit.protoc.gen.server;

import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;

public class TypeMapper {

  // holds the qualified class name to the short class reference
  private final Map<String, String> mapping = new HashMap<>();

  public void add(DescriptorProtos.FileDescriptorProto proto) {
    proto.getMessageTypeList().forEach(m -> {
      mapping.put("." + proto.getPackage() + "." + m.getName(), getClassname(proto) + "." + m.getName());
    });
  }

  public String get(String fqn) {
    return mapping.get(fqn);
  }

  public static String getClassname(DescriptorProtos.FileDescriptorProto proto) {
    String clazz = proto.getOptions().getJavaOuterClassname();

    if (clazz == null || clazz.isEmpty()) {

      char[] classname = proto.getName().substring(0, proto.getName().lastIndexOf('.')).toCharArray();
      StringBuilder sb = new StringBuilder();

      char previous = '_';
      for (char c : classname) {
        if (c == '_') {
          previous = c;
          continue;
        }

        if (previous == '_') {
          sb.append(Character.toUpperCase(c));
        } else {
          sb.append(c);
        }

        previous = c;
      }

      clazz = sb.toString();

      // check to see if there are any messages with this same class name as per java proto specs
      // note that we also check the services too as the protoc compiler does that as well
      for (DescriptorProtos.DescriptorProto type : proto.getMessageTypeList()) {
        if (type.getName().equals(clazz)) {
          return clazz + "OuterClass";
        }
      }

      for (DescriptorProtos.ServiceDescriptorProto service : proto.getServiceList()) {
        if (service.getName().equals(clazz)) {
          return clazz + "OuterClass";
        }
      }
    }

    return clazz;
  }

}
