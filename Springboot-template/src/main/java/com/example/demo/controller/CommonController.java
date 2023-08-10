package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.bean.*;
import com.example.demo.services.*;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.FileUploadUtils;
import com.example.demo.utils.LayuiUtils;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/common")
@Controller
@Api(tags = "通用")
public class CommonController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private UserService userService;
    @Autowired
    private PassForgetService passforgetService;
    @Autowired
    private  JavaMailSenderImpl javaMailSender;
    @Autowired
    private ProducerService productService;
    @Value("${spring.mail.from}")
    private String from;

    String filePath = String.valueOf(Paths.get(FileUploadUtils.realPath, "headers"));

    /**
     * 忘记密码请求
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/toForgetPass")
    public String toForgetPass(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "pass-forget";
    }

    /**
     * 邮箱失效, 忘记密码请求管理员更改密码
     * @param model
     * @param request
     * @return
     */
    @GetMapping("/toPassRequest")
    public String toPassRequest(Model model, HttpServletRequest request){
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        return "pass-request";
    }

    /**
     * 检测邮箱是否存在重复, 所有表 staff admin user 邮箱仅允许存在 1 组
     * @param email
     * @return
     */
    @PostMapping("/checkEmail")
    @ResponseBody
    public LayuiUtils<Staff> checkEmail(String email) {
        return new LayuiUtils<Staff>("未知错误!", null,1,0);
    }

    /**
     * 向管理员递交修改密码请求
     * @param email
     * @param phone
     * @param pass
     * @return
     */
    @PostMapping("/passRequest")
    @ResponseBody
    public LayuiUtils<Staff> passRequestStaff(String email, String phone, String pass) {
        System.out.println(email + phone + pass);
        //根据 Email 在数据库中遍历
        //根据邮箱判断用户是否存在
        //1、职员
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<Staff>();
        queryWrapper.eq(Staff::getEmail,email);
        queryWrapper.eq(Staff::getPhone,phone);
        Staff staff = staffService.getOne(queryWrapper);
        //2、用户
        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<User>();
        queryWrapper1.eq(User::getEmail, email);
        queryWrapper1.eq(User::getPhone, phone);
        User user = userService.getOne(queryWrapper1);
        if(user == null && staff == null){
            return new LayuiUtils<Staff>("邮箱或电话不存在!", null,1,0);
        } else if(staff != null){
            //存入数据库
            passforgetService.save(new PassForget(1, staff.getId(), 0, pass));
            return new LayuiUtils<Staff>("请求成功, 已发起修改请求, 请等待...", null,0,0);
        } else if(user != null){
            //存入数据库
            passforgetService.save(new PassForget(1, user.getId(), 1, pass));
            return new LayuiUtils<Staff>("请求成功, 已发起修改请求, 请等待...", null,0,0);
        }
        return new LayuiUtils<Staff>("未知错误!", null,1,0);
    }

    /**
     * 忘记密码后, 用户通过邮件修改密码
     * @param email
     * @param code
     * @param pass
     * @param model
     * @param request
     * @return
     */
    @PostMapping("/forgetPass")
    @ResponseBody
    public LayuiUtils<Staff> forgetPass(String email, String code, String pass, Model model, HttpServletRequest request) {
        System.out.println(email + code + pass);
        //从 session 中获取验证码
        HttpSession session = request.getSession();
        String serverCode = (String)session.getAttribute("codes");
        if(!code.equalsIgnoreCase(serverCode)){
            return new LayuiUtils<Staff>("验证码错误!", null,1,0);
        }

        //根据 Email 在数据库中遍历
        //根据邮箱判断用户是否存在
        //1、职员
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<Staff>();
        queryWrapper.eq(Staff::getEmail,email);
        Staff staff = staffService.getOne(queryWrapper);
        //2、用户
        LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<User>();
        queryWrapper1.eq(User::getEmail,email);
        User user = userService.getOne(queryWrapper1);
        if(user == null && staff == null){
            return new LayuiUtils<Staff>("邮箱或电话不存在!", null,1,0);
        } else if(staff != null){
            //BCrypt 加密
            staff.setPassword(CommonApi.encodePassword(pass));
            //更新数据库
            staffService.updateById(staff);
            return new LayuiUtils<Staff>("职员密码修改成功", null,0,0);
        } else if(user != null){
            //BCrypt 加密
            user.setPassword(CommonApi.encodePassword(pass));
            //更新数据库
            userService.updateById(user);
            return new LayuiUtils<Staff>("用户密码修改成功", null,0,0);
        }
        return new LayuiUtils<Staff>("未知错误!", null,1,0);
    }

    @GetMapping("/toPassCheck")
    public String toPassCheck(String id, String status, Model model, HttpServletRequest request){
        System.out.println("id:" + id);
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        Map<String, String> params = new HashMap<>(100);
        params.put("id", id);
        params.put("status", status);
        //查找 email
        String email = "";
        switch (status) {
            case "0":
                Admin admin = adminService.getById(id);
                email = admin.getEmail();
                break;
            case "1":
                Staff staff = staffService.getById(id);
                email = staff.getEmail();
                break;
            case "2":
                User user = userService.getById(id);
                email = user.getEmail();
                break;
            default:
                email = "None";
                break;
        }
        params.put("email", email);
        System.out.println("map:" + map.toString());
        model.addAttribute("params", params);
        return "pass-check";
    }

    @PostMapping("checkCode")
    @ResponseBody
    public LayuiUtils<String> checkCode(String code, HttpServletRequest request){
        //从 session 中获取验证码
        HttpSession session = request.getSession();
        String codes = (String)session.getAttribute("codes");

        if(!code.equalsIgnoreCase(codes)){
            System.out.println(codes + code);
            //打印封装数据
            return new LayuiUtils<String>("验证码错误", null,1,0);
        } else {
            //打印封装数据
            return new LayuiUtils<String>("验证成功", null,0,0);
        }
    }

    @PostMapping("sendMail")
    @ResponseBody
    public LayuiUtils<String> sendMail(String to, HttpServletRequest request) throws MessagingException {
        //根据邮箱判断用户是否存在
        //根据类型判断登录
        //1、将所有的用户和管理员信息读取到一起
        List<Admin> admins = adminService.list();
        List<Staff> staffs = staffService.list();
        List<User> users = userService.list();
        Map<String, Integer> map = new HashMap<>(100);
        for(Admin admin: admins){
            map.put(admin.getEmail(), 0);
        }
        for(Staff staff: staffs){
            map.put(staff.getEmail(), 1);
        }
        for(User user: users){
            map.put(user.getEmail(), 2);
        }
        int tag = 0;
        int status = -1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (to.equals(key)) {
                tag = 1;
                status = value;
                break;
            }
        }
        if(tag == 0){
            return new LayuiUtils<String>("邮箱不存在, 请检查注册邮箱信息!", Integer.toString(status),2,0);
        }

        String checkcode = CommonApi.getCheckCode();
        //将验证码放入HttpSession中
        request.getSession().setAttribute("codes",checkcode);

        Mail mail = new Mail(null, to, "登录验证码", checkcode);
        boolean send = productService.send(mail);
        LayuiUtils<String> msg;
        if (send){
            msg =  new LayuiUtils<String>("发送成功", Integer.toString(status),0,0);
        } else {
            msg =  new LayuiUtils<String>("发送失败", Integer.toString(status),1,0);
        }
        //打印封装数据
        return msg;
    }
}
