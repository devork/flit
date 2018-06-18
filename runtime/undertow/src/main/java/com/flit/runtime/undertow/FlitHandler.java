package com.flit.runtime.undertow;

import com.flit.runtime.ErrorCode;
import com.flit.runtime.FlitException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FlitHandler implements HttpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlitHandler.class);

    public static AttachmentKey<String> KEY_METHOD = AttachmentKey.create(String.class);

    private final HttpHandler next;
    private final ConcurrentMap<String, HttpHandler> routes;
    private final ErrorWriter errorWriter;


    private FlitHandler(HttpHandler next, ConcurrentMap<String, HttpHandler> routes, ErrorWriter errorWriter) {
        this.next = next;
        this.routes = routes;
        this.errorWriter = errorWriter;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String path = exchange.getRelativePath();

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String route = path.substring(0, path.lastIndexOf('/'));

        if (!routes.containsKey(route)) {
            if (next != null) {
                next.handleRequest(exchange);
                return;
            }

            errorWriter.write(
                ErrorCode.BAD_ROUTE,
                "No such route: " + route,
                null,
                exchange
            );

            return;
        }

        if (route.length() == path.length()) {
            errorWriter.write(
                ErrorCode.BAD_ROUTE,
                "No method supplied: " + route,
                null,
                exchange
            );
        }

        String method = path.substring(route.length() + 1);
        exchange.putAttachment(KEY_METHOD, method);

        try {
            routes.get(route).handleRequest(exchange);
        } catch (FlitException e) {
            errorWriter.write(e, exchange);
        } catch (Exception e) {
            LOGGER.error("Exception caught at handler: error = {}", e.getMessage(), e);
            errorWriter.write(e, exchange);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpHandler next;
        private ConcurrentMap<String, HttpHandler> routes = new ConcurrentHashMap<>();
        private ErrorWriter errorWriter = new ErrorWriter();

        public Builder withNext(HttpHandler next) {
            this.next = next;
            return this;
        }

        public Builder withRoutes(Map<String, HttpHandler> routes) {
            this.routes = new ConcurrentHashMap<>(routes);
            return this;
        }

        public Builder withRoute(String route, HttpHandler handler) {
            this.routes.put(route, handler);
            return this;
        }

        public FlitHandler build() {
            return new FlitHandler(next, routes, errorWriter);
        }
    }
}
