package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.bean.*;
import com.example.demo.services.CommentService;
import com.example.demo.services.NewsService;
import com.example.demo.services.StaffService;
import com.example.demo.services.UserService;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.DateUtils;
import com.example.demo.utils.LayuiUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FrontController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/front")
@Controller
public class FrontController {
    @Autowired
    private StaffService staffService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;
    @Autowired
    private NewsService newsService;

    @RequestMapping("/login")
    @ResponseBody
    public LayuiUtils<User> login(Admin admin, Model model, HttpServletResponse response){
        System.out.println(admin.toString());
        //根据类型判断登录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, admin.getUsername());
        queryWrapper.eq(User ::getPassword, admin.getPassword());
        User user = userService.getOne(queryWrapper);

        if(user == null) {
            return new LayuiUtils<User>("用户名或密码错误", null,0,0);
        } else {
            //打印封装数据
            model.addAttribute("user", user);
            // create a cookie
            Cookie cookie1 = new Cookie("username", user.getName());
            Cookie cookie2 = new Cookie("token", Integer.toString(user.getId()));

            cookie1.setMaxAge(60 * 10);
            cookie1.setPath("/");
            cookie2.setMaxAge(60 * 10);
            cookie2.setPath("/");
            System.out.println("cookie1"+cookie1.getName() + cookie1.getValue());
            System.out.println("cookie2"+cookie2.getName() + cookie2.getValue());
            response.addCookie(cookie1);
            response.addCookie(cookie2);
            return  new LayuiUtils<User>("登录成功", user,1,0);
        }
    }

    /**
     * 前端主页跳转
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/toFront")
    public String toFront(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/frontMain";
    }

    /**
     * 前台页面的主页 header 模板
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/toHeader")
    public String toHeader(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);

        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/header";
    }

    @RequestMapping("/toAbout")
    public String toAbout(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);

        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/about";
    }

    @RequestMapping("/toInfor")
    public String toInfor(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);

        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/infor";
    }

    @RequestMapping("/toNewsShow/{id}")
    public ModelAndView toNewsShow(@PathVariable("id") int id, Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);

        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        News news = newsService.getById(id);
        //设置模型
        mv.addObject("news", news);
        mv.addObject("time", DateUtils.dateToString(news.getTime()));
        System.out.println(JSON.toJSONString(news));
        //设置视图
        mv.setViewName("news-show");
        return mv;
    }

    @RequestMapping("/toPaper")
    public String toPaper(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/paper";
    }

    @RequestMapping("/toNews")
    public String toNews(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "front/news";
    }

    @RequestMapping("/toContact")
    public String toContact(Model model, HttpServletRequest request){
        List<Staff> staffs = staffService.list();
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        model.addAttribute("staffs", staffs);
        return "front/contact";
    }

    @RequestMapping("/toSigns")
    public String toSigns(){
        return "front/signin";
    }

    @RequestMapping("/toLogin")
    public ModelAndView toLogin(){
        //设置视图
        ModelAndView mv = new ModelAndView();
        mv.setViewName("front/login");
        return mv;
    }

    @RequestMapping("/changeUser")
    public ModelAndView changeUser(HttpServletRequest request, HttpServletResponse response){
        //设置视图
        ModelAndView mv = new ModelAndView();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                System.out.println("Deleting cookie"+ cookie.getName() +"  ,  "+ cookie.getValue());
                response.addCookie(cookie);
            }
        }
        mv.setViewName("front/login");
        return mv;
    }

    @RequestMapping("/saveComment")
    @ResponseBody
    public LayuiUtils<Comment> save(Comment object){
        System.out.println("save:"+object.toString());
        commentService.save(object);
        //打印封装数据
        LayuiUtils<Comment> result = new LayuiUtils<Comment>("保存成功", object,1,0);
        return result;
    }

}
