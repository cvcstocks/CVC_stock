package com.stock.mx2.config;
import com.google.code.kaptcha.util.Config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class GoogleCodeConfig {
    //  <!-- 配置驗證碼 -->
    //    <bean id="captchaProducer" class="com.google.code.kaptcha.impl.DefaultKaptcha">
    //        <property name="config">
    //            <bean class="com.google.code.kaptcha.util.Config">
    //                <constructor-arg>
    //                    <props> <!-- 圖片邊框 -->
    //                        <prop key="kaptcha.border">no</prop>
    //                        <!-- 圖片寬度 -->
    //                        <prop key="kaptcha.image.width">100</prop>
    //                        <!-- 圖片高度 -->
    //                        <prop key="kaptcha.image.height">45</prop>
    //                        <!-- 驗證碼背景顏色漸變，開始顏色 -->
    //                        <!--<prop key="kaptcha.background.clear.from">248,248,248</prop>-->
    //                        <!-- 驗證碼背景顏色漸變，結束顏色 -->
    //                        <!--<prop key="kaptcha.background.clear.to">248,248,248</prop>-->
    //                        <!-- 驗證碼的字符 -->
    //                        <prop key="kaptcha.textproducer.char.string">0123456789abcdefg</prop>
    //                        <!-- 驗證碼字體顏色 -->
    //                        <prop key="kaptcha.textproducer.font.color">red</prop>
    //                        <!-- 驗證碼的效果，水紋 -->
    //                        <!--<prop key="kaptcha.obscurificator.impl">com.google.code.kaptcha.impl.WaterRipple</prop>-->
    //                        <!-- 驗證碼字體大小 -->
    //                        <prop key="kaptcha.textproducer.font.size">33</prop>
    //                        <!-- 驗證碼字數 -->
    //                        <prop key="kaptcha.textproducer.char.length">4</prop>
    //                        <!-- 驗證碼文字間距 -->
    //                        <prop key="kaptcha.textproducer.char.space">5</prop>
    //                        <!-- 驗證碼字體 -->
    //                        <prop key="kaptcha.textproducer.font.names">
    //                            宋體,楷體,微軟雅黑
    //                        </prop>
    //                        <!-- 不加噪聲 -->
    //                        <prop key="kaptcha.noise.impl">com.google.code.kaptcha.impl.NoNoise</prop>
    //                    </props>
    //                </constructor-arg>
    //            </bean>
    //        </property>
    //    </bean>
    @Bean
    public DefaultKaptcha captchaProducer() {

        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.image.width", "100");
        properties.setProperty("kaptcha.image.height", "45");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789abcdefg");
        properties.setProperty("kaptcha.textproducer.font.color", "red");
        properties.setProperty("kaptcha.textproducer.font.size", "33");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.char.space", "5");
        properties.setProperty("kaptcha.textproducer.font.names", "宋體,楷體,微軟雅黑");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        Config config = new Config(properties);

        defaultKaptcha.setConfig(config);



        return defaultKaptcha;
    }
}
