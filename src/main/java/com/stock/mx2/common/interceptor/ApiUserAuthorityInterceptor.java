package com.stock.mx2.common.interceptor;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.stock.mx2.annotation.SameUrlData;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.User;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ApiUserAuthorityInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ApiUserAuthorityInterceptor.class);


    private RedisTemplate<String,String> redisTemplate;

    public ApiUserAuthorityInterceptor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
//        httpServletResponse.setHeader("Access-Control-Allow-Origin",httpServletRequest.getHeader("origin"));
//        //该字段可选，是个布尔值，表示是否可以携带cookie
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT,PATCH, DELETE, OPTIONS");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Headers", "usertoken");
        if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())){
            return  true;
        }

        String url = httpServletRequest.getRequestURI();
        if ("/user/upload.do".equals(url)) {
            return true;
        }
        if ("/user/pay.do".equals(url)) {
            return true;
        }
        if ("/user/newStockBuy.do".equals(url)) {
            return true;
        }
        if ("/user/buchahbds.do".equals(url)) {
            return true;
        }

        User currentUser = getCurrentUser(httpServletRequest);
        if (null == currentUser) {
//            httpServletResponse.reset();
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter writer = httpServletResponse.getWriter();
            Map map = Maps.newHashMap();
            map.put("success", Boolean.valueOf(false));
            map.put("msg", "請先登錄，無權限訪問user");
            writer.print(JsonUtil.obj2String(map));
            writer.flush();
            writer.close();
            return false;
        }
        String uri = httpServletRequest.getRequestURI();


        //验证重复点击与接口权限等
        Boolean checkFlag = checkUri(httpServletResponse, handler, uri,currentUser);
        if (!checkFlag) {
            return Boolean.FALSE;
        }
        //判断请求头
        return true;
    }
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, ModelAndView modelAndView) throws Exception {
    }

    /**
     * token验证
     *
     * @param response 返回
     * @param handler  请求头
     * @return R 的统一返回，并拦截
     * @throws Exception 异常情况，则为服务异常
     */
    private Boolean checkUri(HttpServletResponse response, Object handler, String uri,User user) throws Exception {
        //判断连续请求的情况
        if (handler instanceof HandlerMethod) {
            SameUrlData tokenTypeAnnotation = findAnnotation((HandlerMethod) handler, SameUrlData.class);
            //判断石佛需要验证连续点击
            if (tokenTypeAnnotation != null && redisTemplate.hasKey(user.getId() + uri)) {
                return responseWrite(response, "請勿頻繁點擊");
            } else {
                //将接口+用户信息存储到缓存中。进行重复点击校验
                redisTemplate.opsForValue().set(user.getId()+ uri, "", 30, TimeUnit.SECONDS);
            }
        }
        return true;
    }

    private Boolean responseWrite(HttpServletResponse response, String resultMSG) throws Exception {
        //throw new BaseException(resultCode);
//        定义返回类型为JSON
        response.setContentType("application/json;charset=UTF-8");
//        获取PrintWriter
        PrintWriter out = response.getWriter();
        //将异常类型写入
        ServerResponse<Object> byErrorMsg = ServerResponse.createByErrorMsg(resultMSG);
        out.write(JSON.toJSONString(byErrorMsg));
        //输出流
        out.flush();
        //关闭请求
        out.close();
        return false;
    }

    /**
     * 请求结束后进行的操作
     *
     * @param request  请求信息
     * @param response 返回信息
     * @param handler  请求头
     * @param ex       异常情况
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable
    Exception ex) {
        String uri = request.getRequestURI();
        if (handler instanceof HandlerMethod) {

            User currentUser = getCurrentUser(request);
            SameUrlData tokenTypeAnnotation = findAnnotation((HandlerMethod) handler, SameUrlData.class);
            //没有声明需要权限,或者声明不验证权限
            if (tokenTypeAnnotation != null) {
                redisTemplate.delete(currentUser.getId() + uri);
            }
        }

    }

    /**
     * 获取接口上的注解
     *
     * @param handler        请求方法
     * @param annotationType 想要获取的注解
     * @param <T>            想要获取的注解类型
     * @return 注解
     */
    private <T extends Annotation> T findAnnotation(HandlerMethod handler, Class<T> annotationType) {
        T annotation = handler.getBeanType().getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return handler.getMethodAnnotation(annotationType);
    }



    public User getCurrentUser(HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        System.out.println(property);
        String loginToken = request.getHeader(property);
        if (loginToken == null) {
            System.out.println("loginToken is null");
            return null;
        }
        System.out.println(loginToken);
        String userJson = RedisShardedPoolUtils.get(loginToken);

        if (userJson == null||"".equals(userJson)){
            System.out.println("userJson is null");
            return null;
        }
        System.out.println(userJson);
        return (User) JsonUtil.string2Obj(userJson, User.class);
    }
}
