package com.flit.protoc.gen;

/**
 * Holds a generation buffer used to write basic strings to a wrapper string builder
 */
public class Buffer {

    private StringBuilder buffer;
    private Indent indent;

    public Buffer() {
        this.indent = new Indent();
        this.buffer = new StringBuilder();
    }

    // write
    public Buffer w(String s) {
        buffer.append(s);
        return this;
    }

    // write (multi)
    public Buffer w(String ... ss) {

        for (String s: ss) {
            buffer.append(s);
        }
        return this;
    }

    // indent write
    public Buffer iw(String s) {
        buffer.append(indent).append(s);
        return this;
    }

    // indent write (multi)
    public Buffer iw(String ... ss) {
        buffer.append(indent);

        for (String s: ss) {
            buffer.append(s);
        }
        return this;
    }

    // write newline
    public Buffer wn(String s) {
        buffer.append(s).append("\n");
        return this;
    }

    // write newline (multi)
    public Buffer wn(String ... ss) {

        for (String s: ss) {
            buffer.append(s);
        }
        buffer.append("\n");
        return this;
    }

    // indent write newline
    public Buffer iwn(String s) {
        buffer.append(indent).append(s).append("\n");
        return this;
    }

    // indent write newline (multi)
    public Buffer iwn(String ... ss) {
        buffer.append(indent);

        for (String s: ss) {
            buffer.append(s);
        }
        buffer.append("\n");
        return this;
    }

    // newline
    public Buffer n() {
        buffer.append("\n");
        return this;
    }

    // increase the indentation
    public Buffer inc() {
        indent.inc();
        return this;
    }

    // decrease the indentation
    public Buffer dec() {
        indent.dec();
        return this;
    }

    public String toString() {
        return this.buffer.toString();
    }


}
