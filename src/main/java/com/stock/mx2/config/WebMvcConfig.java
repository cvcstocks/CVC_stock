package com.stock.mx2.config;

import com.stock.mx2.common.interceptor.ApiAdminAuthorityInterceptor;
import com.stock.mx2.common.interceptor.ApiAgentAuthorityInterceptor;
import com.stock.mx2.common.interceptor.ApiUserAuthorityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedMethods("*")
//                .allowCredentials(true)  // 允許帶cookie訪問
//                .allowedHeaders("*")
//                .maxAge(3600);
//                ;
//
//    }



    //註冊攔截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //SpringMVC下，攔截器的註冊需要排除對靜態資源的攔截(*.css,*.js)
        //SpringBoot已經做好了靜態資源的映射，因此我們無需任何操作


        registry.addInterceptor(new ApiAgentAuthorityInterceptor()).addPathPatterns("/agentNew/**")
                .excludePathPatterns("/index.html", "/", "/user/login")
        ;
        registry.addInterceptor(new ApiUserAuthorityInterceptor(redisTemplate)).addPathPatterns("/user/**")
                .excludePathPatterns("/index.html", "/", "/user/login")
        ;
        registry.addInterceptor(new ApiAdminAuthorityInterceptor()).addPathPatterns("/admin/**")
                .excludePathPatterns("/index.html", "/", "/user/login","/user/futures/position/index.do")
        ;
    }
}
