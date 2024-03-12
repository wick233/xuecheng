package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();
        //允许域名跨域调用
        config.addAllowedOrigin("*");
        //允许跨域发送cookie
        config.setAllowCredentials(true);
        //允许放行的请求头
        config.addAllowedHeader("*");
        //允许跨域方法
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return new CorsFilter(source);
    }

}
