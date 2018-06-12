package com.flit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Strings {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Reads the given input stream to a string using UTF8 charset
     *
     * @param is    The input stream
     * @return  The decoded string
     */
    public static String read(InputStream is) {

        if (is == null) {
            throw FlitException.builder()
                .withMessage("No input stream specified")
                .withErrorCode(ErrorCode.INTERNAL)
                .build();
        }

        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8))) {
            int c;
            while ((c = br.read()) != -1) {
                sb.append((char) c);
            }

            return sb.toString();
        } catch (IOException e) {
            throw FlitException.builder()
                .withMessage("Failed to read input stream")
                .withErrorCode(ErrorCode.INTERNAL)
                .withCause(e)
                .build();
        }

    }

}
