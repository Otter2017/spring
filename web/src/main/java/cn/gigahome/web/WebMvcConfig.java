package cn.gigahome.web;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        assert registry != null;
        registry.addInterceptor(new MethodInterceptor()).addPathPatterns("/**");
    }
}
