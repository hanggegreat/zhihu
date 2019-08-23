package cn.lollipop.zhihu.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import cn.lollipop.zhihu.interceptor.LoginRequiredInterceptor;
import cn.lollipop.zhihu.interceptor.PassportInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    private final PassportInterceptor passportInterceptor;
    private final LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    public WebConfiguration(PassportInterceptor passportInterceptor, LoginRequiredInterceptor loginRequiredInterceptor) {
        this.passportInterceptor = passportInterceptor;
        this.loginRequiredInterceptor = loginRequiredInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor).addPathPatterns("/**");
        registry.addInterceptor(loginRequiredInterceptor).addPathPatterns("/user/**");
    }
}
