package com.stock.mx2.xss;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import org.springframework.web.servlet.DispatcherServlet;

import org.springframework.web.servlet.HandlerExecutionChain;

@Component
public class DispatcherServletWrapper extends DispatcherServlet {

    protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        HandlerExecutionChain chain = super.getHandler(request);
        Object handler = chain.getHandler();
        if (!(handler instanceof HandlerMethod)) {
            return chain;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        if (!hm.getBeanType().isAnnotationPresent(org.springframework.stereotype.Controller.class)) {
            return chain;
        }
        return new com.stock.mx2.security.xss.HandlerExecutionChainWrapper(chain, request, getWebApplicationContext());
    }
}
