package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//开启拦截器
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //授权规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        //请求授权的规则
//        http.authorizeRequests()
//                .antMatchers("/").permitAll()
//                .antMatchers("/level1/**").hasRole("vip1")
//                .antMatchers("/level2/**").hasRole("vip2")
//                .antMatchers("/level3/**").hasRole("vip3");

        //没有权限默认跳转登录页
        //定制登录页
        http.formLogin().loginPage("/toLogin").usernameParameter("user").passwordParameter("pwd").loginProcessingUrl("/login");
        //开启注销
        http.logout().logoutSuccessUrl("/security");
        //开启记住我 cookie 默认保存两周
        http.rememberMe().rememberMeParameter("remember");
        http.headers().frameOptions().sameOrigin();
        http.csrf().disable();
    }

//    //认证规则
//    //密码需要加密
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //类引用
//        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
//                .withUser("vip1").password(new BCryptPasswordEncoder().encode("1")).roles("vip1")
//                .and()
//                .withUser("vip2").password(new BCryptPasswordEncoder().encode("1")).roles("vip2")
//                .and()
//                .withUser("vip3").password(new BCryptPasswordEncoder().encode("1")).roles("vip3")
//                .and()
//                .withUser("root").password(new BCryptPasswordEncoder().encode("1")).roles("vip1","vip2","vip3");
//
//        //数据库引用
//
//    }

    /**
     * 创建BCryptPasswordEncoder注入容器
     * @return
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
