package com.flit.runtime.spring;

import com.flit.runtime.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FlitHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlitHandlerExceptionResolver.class);

    private View view = new MappingJackson2JsonView();

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        LOGGER.warn("Handling internal exception: error = {}", ex.getMessage());

        ModelAndView model = new ModelAndView();
        model.setView(view);

        if (ex instanceof NoHandlerFoundException) {
            response.setStatus(ErrorCode.BAD_ROUTE.getHttpStatus());
            model.addObject("code", ErrorCode.BAD_ROUTE);
            model.addObject("msg", "No handler found for the given call");
            return model;
        }

        response.setStatus(ErrorCode.UNKNOWN.getHttpStatus());
        model.addObject("code", ErrorCode.UNKNOWN);
        model.addObject("msg", "An unknown error occurred within the application");

        return model;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
