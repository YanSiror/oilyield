package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Mail;
import com.example.demo.bean.Staff;
import com.example.demo.bean.User;
import com.example.demo.services.AdminService;
import com.example.demo.services.ProducerService;
import com.example.demo.services.StaffService;
import com.example.demo.utils.*;
import io.swagger.annotations.Api;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
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
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * AdminController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/admin")
@Controller
@Api(tags = "管理员")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private  JavaMailSenderImpl javaMailSender;
    @Autowired
    private ProducerService productService;
    @Value("${spring.mail.from}")
    private String from;

    String filePath = String.valueOf(Paths.get(FileUploadUtils.realPath, "headers"));

    @GetMapping("/toMain")
    public String toMain(Admin admin, Model model){
        System.out.println(admin);
        model.addAttribute("admin", admin);
        return "main-admin";
    }

    @GetMapping("/toStaffMain")
    public String toStaffMain(Admin admin, Model model){
        System.out.println(admin);
        model.addAttribute("admin", admin);
        return "main-staff";
    }

    @GetMapping("/toHeader")
    public String toHeader(String id, Model model){
        System.out.println(id);
        model.addAttribute("id", id);
        return "upload-header";
    }

    @GetMapping("/toSignin")
    public String toSignin(String email, Model model){
        return "signin";
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request){
        ModelAndView mv = new ModelAndView();
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("loginUser");
        mv.setViewName("index");
        return mv;
    }

    @PostMapping("/login")
    @ResponseBody
    public LayuiUtils<Admin> login(Admin admin,Model model, HttpServletRequest request){
        String type = request.getParameter("type");
        System.out.println(admin.toString());
        System.out.println(type);

        //MD5加密
//        String md5Password = DigestUtils.md5DigestAsHex(admin.getPassword().getBytes());
//        System.out.println(admin.getPassword() + ", " + md5Password);
//        admin.setPassword(md5Password);
        Admin user = new Admin();
        boolean isValid = false;
        if(Integer.parseInt(type) == 0){
            //根据类型判断登录
            LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Admin::getUsername,admin.getUsername());
            user = adminService.getOne(queryWrapper);
            if(user != null){
                isValid = CommonApi.validatePassword(admin, user.getPassword());
                System.out.println(isValid);
            }
        } else if(Integer.parseInt(type) == 1){
            LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Staff::getId, Integer.parseInt(admin.getUsername()));
            Staff staff = staffService.getOne(queryWrapper);
            if(staff == null){
                return new LayuiUtils<Admin>("请检查身份设置", user,0,0);
            } else {
                isValid = CommonApi.validatePassword(admin, staff.getPassword());
                System.out.println(isValid);
            }
            System.out.println(staff.toString());
            user.setUsername(staff.getName());
            user.setPassword(staff.getPassword());
            user.setId(staff.getId());
        }

        //获取数据
        String verifycode = request.getParameter("verifycode");
        //验证码检验
        HttpSession session = request.getSession();
        //强转 获取用户填写的验证码
        String checkCode = (String)session.getAttribute("CHECKCODE_SERVER");
        //将已使用的验证码从session中移除,确保验证码单次
        session.removeAttribute("CHECKCODE_SERVER");

        //验证码不正确
        LayuiUtils<Admin> result = null;
        if(checkCode == null){
            result = new LayuiUtils<Admin>("无验证码信息", user,0,0);
        }
        else if(!isValid) {
            //打印封装数据
            result = new LayuiUtils<Admin>("用户名或密码错误", null,0,0);
            return result;
        }
        else if(!checkCode.equalsIgnoreCase(verifycode)){
            System.out.println(verifycode + checkCode);
            result = new LayuiUtils<Admin>("验证码错误", user,0,0);
            return result;
        }
        else {
            //打印封装数据
            session.setAttribute("loginUser",admin.getUsername());

//            String tocken = JWTUtils.getToken(admin);       //设置 JWT token, 用于验证登录
//            model.addAttribute("userTocken", tocken);

            model.addAttribute("admin", admin);
            if(Integer.parseInt(type) == 0){
                result = new LayuiUtils<Admin>("登录成功", user,1,0);
            }
            else if(Integer.parseInt(type) == 1) {
                result = new LayuiUtils<Admin>("登录成功", user,2,0);
            }
            return result;
        }
        return result = new LayuiUtils<Admin>("未知错误", user,0,0);
    }

    /**
     * 采用分页代码方法
     * @param adminId
     * @return ModelAndView
     */
    @GetMapping("/list/{adminId}")
    public ModelAndView list(@PathVariable("adminId") int adminId) {
        ModelAndView mv = new ModelAndView();
        //紧跟的第一个select方法被分页
        Admin admin = adminService.getById(adminId);
        mv.addObject("admin", admin);
        System.out.println(admin.toString());
        mv.setViewName("admin-list");
        return mv;
    }

    /**
     * //上传文件
     * @param uploadFile
     * @throws IOException
     */
    @PostMapping("/uploadHead")
    public void uploadHead(MultipartFile uploadFile) throws IOException{
        // 创建文件路径
        File file = new File(filePath);
        //如果 module文件夹不存在,创建文件夹
        if(!file.exists()){
            file.mkdir();
        }

        String name;        //文件名
        //获取上传文件的名称
        name = uploadFile.getOriginalFilename();
        //保存到固定位置
        Path path = Paths.get(filePath, name);
        uploadFile.transferTo(new File(String.valueOf(path)));
        //上传到服务器
        System.out.println(filePath);
        boolean flag = FtpFileUtil.uploadToFtp(name, uploadFile.getInputStream());
        if(flag){
            System.out.println("ftp上传成功！" + name);
        }
    }


    /**
     * 保存头像
     * @param id
     * @param filename
     * @return
     */
    @PostMapping("/saveHeader")
    @ResponseBody
    public LayuiUtils<Admin> saveHeader(String id, String filename){
        Admin admin = new Admin(Integer.parseInt(id), filename);
        System.out.println("filename: " + filename);
        adminService.updateById(admin);
        System.out.println(admin.toString());
        //打印封装数据
        return new LayuiUtils<Admin>("保存成功", admin,1,0);
    }

    /**
     * 获取头像
     * @param id
     * @param response
     * @throws IOException
     */
    @GetMapping("/getHeader")
    public void getHeader(String id, HttpServletResponse response) throws IOException {
        //服务器通知浏览器不要缓存
        response.setHeader("pragma","no-cache");
        response.setHeader("cache-control","no-cache");
        response.setHeader("expires","0");

        System.out.println(id);
        Admin admin = adminService.getById(Integer.parseInt(id));
        System.out.println(admin.getHeader());
        // 创建文件路径
        Path path = Paths.get(filePath,admin.getHeader());
        System.out.println(path);

        File sourceimage = new File(String.valueOf(path));
        BufferedImage image = ImageIO.read(sourceimage);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }

    @PutMapping("/modify")
    @ResponseBody
    public LayuiUtils<Admin> modify(Admin admin){
        //MD5加密 - 对修改的密码进行加密
        //admin.setPassword(DigestUtils.md5DigestAsHex(admin.getPassword().getBytes()));

        //BCrypt 加密
        admin.setPassword(CommonApi.encodePassword(admin.getPassword()));
        System.out.println("controller"+ admin.toString());
        adminService.updateById(admin);
        //打印封装数据
        return new LayuiUtils<Admin>("修改成功!", admin,0,0);
    }

    @GetMapping("/loadData/{adminId}")
    public ModelAndView loadData(@PathVariable("adminId") int adminId){
        ModelAndView mv = new ModelAndView();
        Admin admin = adminService.getById(adminId);
        //设置模型
        mv.addObject("admin", admin);
        //设置视图
        mv.setViewName("admin-modify");
        return mv;
    }

    @PostMapping("/signIn")
    @ResponseBody
    public LayuiUtils<Admin> signIn(Admin admin,Model model, HttpServletRequest request){
        Random random = new Random();
        int number = random.nextInt(40) + 1;
        System.out.println(admin);
        System.out.println(admin.toString());
        admin.setHeader(Integer.toString(number) + ".jpg");

        //使用 SpringSecurity 提供的 BCryptPasswordEncoder 进行加密
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encodePass = passwordEncoder.encode(admin.getPassword());
//        System.out.println(admin.getPassword() + ", " + encodePass);
//        admin.setPassword(encodePass);

        // md5 加密
//        String md5Password = DigestUtils.md5DigestAsHex(admin.getPassword().getBytes());
//        System.out.println(admin.getPassword() + ", " + md5Password);
//        admin.setPassword(md5Password);

        //验证 邮箱 和 用户名
        if(adminService.findByEmail(admin.getEmail())){
            return new LayuiUtils<Admin>("该邮箱已存在!", admin,1,0);
        }

        //BCrypt 加密
        admin.setPassword(CommonApi.encodePassword(admin.getPassword()));

        adminService.save(admin);
        System.out.println(admin.toString());
        //打印封装数据
        HttpSession session = request.getSession();
        session.setAttribute("loginUser",admin.getUsername());
        model.addAttribute("admin", admin);
        return new LayuiUtils<Admin>("注册成功", admin,0,0);
    }

    @PostMapping("send")
    @ResponseBody
    public LayuiUtils<String> sendMail(String to,String type, HttpServletRequest request) throws MessagingException {
        //根据邮箱判断用户是否存在
        //根据类型判断登录
        //1、将所有的用户和管理员信息读取到一起
        List<Admin> admins = adminService.list();
        List<Staff> staffs = staffService.list();
        Map<String, Integer> map = new HashMap<>(1000);
        for(Admin admin: admins){
            map.put(admin.getEmail(), 0);
        }
        for(Staff staff: staffs){
            map.put(staff.getEmail(), 1);
        }
        int tag = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if(to.equals(key)){
                tag = 1;
            }
            if(to.equals(key) && value != Integer.parseInt(type)){
                return new LayuiUtils<String>("用户组错误, 请重新选择!", null,1,0);
            }
        }
        if(tag == 0){
            return new LayuiUtils<String>("用户不存在, 请完成注册!", null,2,0);
        }


        String checkcode = CommonApi.getCheckCode();
        //将验证码放入HttpSession中
        request.getSession().setAttribute("codes",checkcode);

        Mail mail = new Mail(null, to, "登录验证码", checkcode);
        boolean send = productService.send(mail);
        LayuiUtils<String> msg;
        if (send){
            msg =  new LayuiUtils<String>("发送成功", null,0,0);
        } else {
            msg =  new LayuiUtils<String>("发送失败", null,1,0);
        }
        //打印封装数据
        return msg;
    }

    @PostMapping("/sendCode")
    @ResponseBody
    public LayuiUtils<String> sendCode(String mail, HttpServletRequest request) throws MessagingException {
        //根据邮箱判断用户是否存在
        //根据类型判断登录
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getEmail,mail);
        Admin admin = adminService.getOne(queryWrapper);
        if(admin == null){
            return new LayuiUtils<String>("用户不存在, 请完成注册!", null,0,0);
        }

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        // 标题
        helper.setSubject("验证码");
        String checkcode = CommonApi.getCheckCode();

        //将验证码放入HttpSession中
        request.getSession().setAttribute("codes",checkcode);
        // 内容, 第二个参数为true则以html方式发送, 否则以普通文本发送
        helper.setText("<h1 style='red'>" + checkcode + "</h1>", true);
        // 收件人
        helper.setTo(mail);
        // 发件人
        helper.setFrom(from);
        // 发送
        javaMailSender.send(message);
        //打印封装数据
        return new LayuiUtils<String>("发送成功", null,1,0);
    }

    @PostMapping("/loginWithCode")
    @ResponseBody
    public LayuiUtils<Admin> loginWithCode(String email, String code, Model model, HttpServletRequest request){
        //根据邮箱判断用户是否存在
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getEmail,email);
        Admin admin = adminService.getOne(queryWrapper);

        //从 session 中获取验证码
        HttpSession session = request.getSession();
        String codes = (String)session.getAttribute("codes");

        LayuiUtils<Admin> result;
        if(admin == null){
            result = new LayuiUtils<Admin>("用户不存在,请完成注册!", null,0,0);
            return result;
        }
        //验证码错误
        else if(!code.equalsIgnoreCase(codes)){
            System.out.println(codes + code);
            //打印封装数据
            result = new LayuiUtils<Admin>("验证码错误", null,-1,0);
            return result;
        } else{
            //打印封装数据
            session.setAttribute("loginUser",admin.getUsername());
            model.addAttribute("admin", admin);
            result = new LayuiUtils<Admin>("登陆成功", admin,1,0);
            return result;
        }
    }

    @GetMapping("/checkcode")
    public void checkcode(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ModelAndView mv = new ModelAndView();

        //服务器通知浏览器不要缓存
        response.setHeader("pragma","no-cache");
        response.setHeader("cache-control","no-cache");
        response.setHeader("expires","0");

        //在内存中创建一个长80，宽30的图片，默认黑色背景
        //参数一：长
        //参数二：宽
        //参数三：颜色
        int width = 80;
        int height = 30;
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        //获取画笔
        Graphics g = image.getGraphics();
        //设置画笔颜色为灰色
        g.setColor(Color.darkGray);
        //填充图片
        g.fillRect(0,0, width,height);

        //产生4个随机验证码，12Ey
        String checkcode = CommonApi.getCheckCode();
        //将验证码放入HttpSession中
        request.getSession().setAttribute("CHECKCODE_SERVER",checkcode);

        //设置画笔颜色为黄色
        g.setColor(Color.orange);
        //设置字体的小大
        g.setFont(new Font("黑体",Font.BOLD,24));
        //向图片上写入验证码
        g.drawString(checkcode,15,25);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }
}
