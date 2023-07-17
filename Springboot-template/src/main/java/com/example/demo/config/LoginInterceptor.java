package com.example.demo.config;

import com.example.demo.bean.Admin;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object admin = request.getSession().getAttribute("loginUser");
        String language = request.getParameter("language");
        if(admin == null){
//            if(language.equals("en_US"))
//                request.setAttribute("login_msg","No permission, please log in first!");
//            else
            request.setAttribute("login_msg","没有权限,请先登录!");
            request.getRequestDispatcher("/security.html").forward(request,response);
            return false;
        } else {
            return true;
        }
    }
}
