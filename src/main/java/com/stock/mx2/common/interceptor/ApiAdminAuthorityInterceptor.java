package com.stock.mx2.common.interceptor;


import com.google.common.collect.Maps;
import com.stock.mx2.pojo.SiteAdmin;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.ip.IpUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ApiAdminAuthorityInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ApiAdminAuthorityInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {

//        //表示接受任意域名的请求,也可以指定域名
//        httpServletResponse.setHeader("Access-Control-Allow-Origin",httpServletRequest.getHeader("origin"));
//        //该字段可选，是个布尔值，表示是否可以携带cookie
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT,PATCH, DELETE, OPTIONS");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "admintoken");
        if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())){
            return true;
        }


        SiteAdmin siteAdmin = null;
        String loginToken = httpServletRequest.getHeader(PropertiesUtil.getProperty("admin.cookie.name"));
        if (StringUtils.isNotEmpty(loginToken)) {
            String adminJsonStr = RedisShardedPoolUtils.get(loginToken);
            if (adminJsonStr==null || "".equals(adminJsonStr)) {
                siteAdmin = null;
            }else{
                siteAdmin = (SiteAdmin) JsonUtil.string2Obj(adminJsonStr, SiteAdmin.class);
            }
        }
        if (null == siteAdmin) {
//            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = httpServletResponse.getWriter();
            Map map = Maps.newHashMap();
            map.put("success", Boolean.valueOf(false));
            map.put("msg", "請先登錄，無權限訪問admin");
            writer.print(JsonUtil.obj2String(map));
            writer.flush();
            writer.close();
            return false;
        }

        //194.26.73.150, 172.70.34.195
        String ip = IpUtils.getIp(httpServletRequest);

        String[] split = ip.split(", ");
        if (ip!=null){
            return true;
        }
        for (String s : split) {
            if (s.equals("118.140.35.45")){
                return true;
            }
            if (s.equals("118.140.35.50")){
                return true;
            }
            if (s.equals("58.152.85.239")){
                return true;
            }
            if (s.equals("66.249.77.78")){
                return true;
            }
            if (s.equals("220.133.13.177")){
                return true;
            }
            if (s.equals("111.241.195.2")){
                return true;
            }
            if (s.equals("194.26.73.150")){
                return true;
            }
            if (s.equals("160.16.103.240")){
                return true;
            }
            if (s.equals("194.26.73.150")){
                return true;
            }
            if (s.equals("101.24.91.83")){
                return true;
            }  if (s.equals("111.90.140.138")){
                return true;
            }  if (s.equals("103.233.2.196")){
                return true;
            }
            //2022-12-09 杨总让加了个美国IP
            if (s.equals("144.34.183.201")){
                return true;
            }
            if (s.equals("66.112.212.211")){
                return true;
            }
            if (s.equals("144.34.171.137")){
                return true;
            }
            if (s.equals("144.34.188.53")){
                return true;
            }
            //虎虎子
            if (s.equals("219.76.157.115")){
                return true;
            }
            //虎虎子
            if (s.equals("58.152.85.37")){
                return true;
            }
            //二次元大哥
            if (s.equals("36.227.240.55")){
                return true;
            }
            //二次元大哥
            if (s.equals("8.39.126.101")){
                return true;
            }
            //二次元大哥
            if (s.equals("36.227.241.216")){
                return true;
            }

        }


        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = httpServletResponse.getWriter();
        Map map = Maps.newHashMap();
        map.put("success", Boolean.valueOf(false));
        map.put("msg", "請先登錄，無權限訪問admin"+ip);
        writer.print(JsonUtil.obj2String(map));
        writer.flush();
        writer.close();
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception {
    }
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {
    }
}
