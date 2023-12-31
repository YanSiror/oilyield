package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Comment;
import com.example.demo.bean.Staff;
import com.example.demo.bean.User;
import com.example.demo.services.UserService;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.LayuiUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * UserController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/user")
@Controller
@Api(tags = "用户")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/save")
    @ResponseBody
    public LayuiUtils<List<User>> save(User object){
        System.out.println("save:"+object.toString());
        //BCrypt 加密
        object.setPassword(CommonApi.encodePassword(object.getPassword()));
        userService.save(object);
        //打印封装数据
        LayuiUtils<List<User>> result = new LayuiUtils<List<User>>("1", null,1,0);
        return result;
    }

    @PutMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<User>> modify(User user){
        System.out.println("modify:"+user.toString());
        //BCrypt 加密
        user.setPassword(CommonApi.encodePassword(user.getPassword()));
        //更新数据库
        userService.updateById(user);
        //打印封装数据
        LayuiUtils<List<User>> result = new LayuiUtils<List<User>>("1", null,1,0);
        return result;
    }


    @GetMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        User user = userService.getById(id);
        //设置模型
        mv.addObject("user", JSON.toJSONString(user));
        System.out.println(JSON.toJSONString(user));
        //设置视图
        mv.setViewName("user-modify");
        return mv;
    }

    @GetMapping("/seeData/{id}")
    public ModelAndView seeData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        User user = userService.getById(id);
        //设置模型
        mv.addObject("user", JSON.toJSONString(user));
        System.out.println(JSON.toJSONString(user));
        //设置视图
        mv.setViewName("user-see");
        return mv;
    }

    /**
     * 用户删除
     * @param ids
     * @return
     * @throws Exception
     */
    @GetMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<User>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        userService.deleteSelected(ids);
        return new LayuiUtils<List<User>>("1", null,1,0);
    }

    @GetMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<User>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        userService.removeById(id);
        //打印封装数据
        LayuiUtils<List<User>> result = new LayuiUtils<List<User>>("1", null,1,0);
        return result;
    }

    @GetMapping("/toList")
    public String toList(){
        return "user-list";
    }

    @GetMapping("/toAdd")
    public String toAdd(){
        return "user-add";
    }

    /**
     * 采用分页代码方法, 遍历 User
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public LayuiUtils<List<User>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(User::getName);
        //进行分页查询
        Page<User> pageinfo = new Page<User>(page, size);
        userService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<User>> result = new LayuiUtils<List<User>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @GetMapping("/search")
    @ResponseBody
    public LayuiUtils<List<User>> search(String name, String position){
        System.out.println("search:"+name + position);

        //实际的查找逻辑
        User user = new User();
        //设置参数
        user.setName(name);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<User>();
        lambdaQueryWrapper.like(name != null, User::getName,name);
        List<User> userList = userService.list(lambdaQueryWrapper);
        //使用PageInfo包装数据
        PageInfo<User> pageInfo = new PageInfo<User> (userList,3);
        //打印封装数据
        LayuiUtils<List<User>> result = new LayuiUtils<List<User>>("", userList,0,(int)pageInfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }

    @GetMapping("/signIn")
    @ResponseBody
    public LayuiUtils<User> signIn(User user1, Model model, HttpServletRequest request, HttpServletResponse response){
        System.out.println("save:" + user1.toString());

        //验证 邮箱 和 用户名
        if(userService.findByEmail(user1.getEmail())){
            return new LayuiUtils<User>("该邮箱已存在!", user1,1,0);
        }
        if(userService.findByPhone(user1.getPhone())){
            return new LayuiUtils<User>("该手机号已存在!", user1,1,0);
        }

        //BCrypt 加密
        user1.setPassword(CommonApi.encodePassword(user1.getPassword()));
        userService.save(user1);
        System.out.println(user1.toString());


        //根据类型判断自增id
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getPhone, user1.getPhone());
        queryWrapper.eq(User ::getPassword, user1.getPassword());
        User user = userService.getOne(queryWrapper);
        if(user != null) {
            // create a cookie
            Cookie cookie1 = new Cookie("username", user.getPhone());
            Cookie cookie2 = new Cookie("token", Integer.toString(user.getId()));
            cookie1.setMaxAge(60 * 10);
            cookie1.setPath("/");
            cookie1.setMaxAge(60 * 10);
            cookie2.setPath("/");
            System.out.println("cookie1" + cookie1.getName() + cookie1.getValue());
            System.out.println("cookie2" + cookie2.getName() + cookie2.getValue());
            response.addCookie(cookie1);
            response.addCookie(cookie2);
            return new LayuiUtils<User>("注册成功", user, 0, 0);
        }
        return new LayuiUtils<User>("注册失败", null,1,0);
    }
    
}
