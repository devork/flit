package com.flit.protoc.gen;

public class Indent {
  private final String indent;
  private String current = "";

  public Indent() {
    this.indent = "    ";
  }

  public Indent(String indent) {
    this.indent = indent;
  }

  public String inc() {
    current += indent;
    return current;
  }

  public String dec() {
    if (current.isEmpty()) {
      return current;
    }

    current = current.substring(indent.length());
    return current;
  }

  public String toString() {
    return current;
  }
}
