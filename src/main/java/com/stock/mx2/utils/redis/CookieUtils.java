package com.stock.mx2.utils.redis;


import com.stock.mx2.utils.PropertiesUtil;

import javax.servlet.http.Cookie;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;


public class CookieUtils {

    private static final Logger log = LoggerFactory.getLogger(CookieUtils.class);


    private static final String COOKIE_DOMAIN = PropertiesUtil.getProperty("cookie.project.url");


    public static void writeLoginToken(HttpServletResponse httpServletResponse, String token, String COOKIE_NAME) {

        Cookie cookie = new Cookie(COOKIE_NAME, token);


        cookie.setPath("/");


        cookie.setMaxAge(99999999);


        cookie.setHttpOnly(true);
        //cookie.setHttpOnly(false);

        log.info("write cookie name :{} ,cookie value : {}", cookie.getName(), cookie.getValue());

        httpServletResponse.addCookie(cookie);

    }


    public static String readLoginToken(HttpServletRequest httpServletRequest, String COOKIE_NAME) {


        return httpServletRequest.getHeader(COOKIE_NAME);

    }


    public static void delLoginToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String COOKIE_NAME) {

        Cookie[] cookies = httpServletRequest.getCookies();

        if (cookies != null)

            for (Cookie ck : cookies) {

                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {


                    ck.setPath("/");

                    ck.setMaxAge(0);

                    log.info("del cookie name : {} ,cookie value : {}", ck.getName(), ck.getValue());

                    httpServletResponse.addCookie(ck);

                    return;

                }

            }

    }

}
