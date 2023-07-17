package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Mail;
import com.example.demo.bean.Staff;
import com.example.demo.bean.User;
import com.example.demo.services.AdminService;
import com.example.demo.services.ProducerService;
import com.example.demo.services.StaffService;
import com.example.demo.services.UserService;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.FileUploadUtils;
import com.example.demo.utils.FtpFileUtil;
import com.example.demo.utils.LayuiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RequestMapping("/common")
@Controller
public class CommonController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private UserService userService;
    @Autowired
    private  JavaMailSenderImpl javaMailSender;
    @Autowired
    private ProducerService productService;
    @Value("${spring.mail.from}")
    private String from;

    String filePath = String.valueOf(Paths.get(FileUploadUtils.realPath, "headers"));

    @RequestMapping("/toPassCheck")
    public String toPassCheck(String id, String status, Model model, HttpServletRequest request){
        System.out.println("id:" + id);
        Map<String, String> map = CommonApi.getCookie(request);
        System.out.println("map:" + map.toString());
        model.addAttribute("map", map);
        Map<String, String> params = new HashMap<>();
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
        Map<String, Integer> map = new HashMap<>();
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
