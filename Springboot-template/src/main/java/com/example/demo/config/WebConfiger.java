package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiger implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/index.html").setViewName("index");
        registry.addViewController("/security.html").setViewName("index");
        registry.addViewController("/loginwithcode.html").setViewName("loginwithcode");
        registry.addViewController("/main.html").setViewName("main-admin");
        registry.addViewController("/staff-list.html").setViewName("staff-list");
    }

    //自定义国际化组件
    @Bean
    public LocaleResolver localeResolver() {
        return new MyLocaleResolver();
    }

    //拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).
                addPathPatterns("/**").
                excludePathPatterns("/security.html","/",
                        "/admin/checkcode","/admin/checkcode","/admin/loginWithCode","/admin/sendCode","/admin/send",
                        "/admin/login", "/admin/login","/admin/toSignin","/admin/signIn",
                        "/staff/toSignin", "/staff/save", "/staff/login", "/staff/saveComment",
                        "/user/**", "/news/**", "/common/**",
                        "/front/**","/product/**","/assets/**","/uploads/**",
                        "/loginwithcode.html", "/signin.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:C:/header/");
        //下面这个写法得到的值与上面的效果一样
        //.addResourceLocations("file:C:"+System.getProperty("file.separator")+"Pic"+System.getProperty("file.separator"))
    }
}