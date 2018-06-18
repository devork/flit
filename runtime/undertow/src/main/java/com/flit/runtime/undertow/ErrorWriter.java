package com.flit.runtime.undertow;

import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;
import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.util.HashMap;
import java.util.Map;

public class ErrorWriter {
    private final Gson gson;

    public ErrorWriter() {
        gson = new Gson();
    }

    public void write(ErrorCode errorCode, String message, Map<String, Object> meta, HttpServerExchange exchange) {
        Map<String, Object> err = new HashMap<>();
        err.put("code", errorCode.getErrorCode());
        err.put("msg", message);

        if (meta != null) {
            err.put("meta", meta);
        }

        exchange.setStatusCode(errorCode.getHttpStatus());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(gson.toJson(err));
    }

    public void write(FlitException e, HttpServerExchange exchange) {
        write(e.getErrorCode(), e.getMessage(), e.getMeta(), exchange);
    }

    public void write(Exception e, HttpServerExchange exchange) {
        write(ErrorCode.INTERNAL, "An internal error occurred", null, exchange);
    }
}
