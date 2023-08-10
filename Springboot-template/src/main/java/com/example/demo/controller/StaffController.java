package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Staff;
import com.example.demo.bean.Yield;
import com.example.demo.services.StaffService;
import com.example.demo.utils.BloomFilterUtil;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.LayuiUtils;
import com.example.demo.utils.RedisUtils;
import com.github.pagehelper.PageInfo;
import io.lettuce.core.RedisConnectionException;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StaffController  在该文件集成了 Redis 缓存
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/staff")
@Controller
@Api(tags = "职员")
public class StaffController {
    @Autowired
    private StaffService staffService;
    @Autowired
    private CacheManager cacheManager;
    @Resource
    private RedisUtils redisUtils;
    @Autowired
    private BloomFilterUtil bloomFilter;
    private final String redisCache = "staff" + "Cache";
    private final String redisPage = "staff" + "Page";

    @PostMapping("/save")
    @ResponseBody
    public LayuiUtils<Staff> save(Staff object, HttpServletRequest request){
        System.out.println("save:"+object.toString());
        //MD5加密 - 对修改的密码进行加密
//        String md5Password = DigestUtils.md5DigestAsHex(object.getPassword().getBytes());
//        System.out.println(object.getPassword() + ", " + md5Password);
//        object.setPassword(md5Password);
        //验证 邮箱 和 用户名
        if(staffService.findByEmail(object.getEmail())){
            return new LayuiUtils<Staff>("该邮箱已存在!", object,1,0);
        }
        if(staffService.findByPhone(object.getPhone())){
            return new LayuiUtils<Staff>("该手机号已存在!", object,1,0);
        }
        //BCrypt 加密
        object.setPassword(CommonApi.encodePassword(object.getPassword()));

        //保存到数据库
        staffService.save(object);
        //保存到 redis
        redisUtils.set("staffCache::" + object.getId(), object);
        //保存到 布隆过滤器
        bloomFilter.add(Integer.toString(object.getId()));
        redisUtils.clearPageCache("staffPage");

        //添加登录用户
        HttpSession session = request.getSession();
        session.setAttribute("loginUser",object.getName());
        return new LayuiUtils<Staff>("注册成功", object,0,0);
    }

    @PutMapping("/modify")
    @ResponseBody
    @CacheEvict(value = "staffCache", allEntries = true)  //清理 setmeal 缓存下的所有数据
    public LayuiUtils<List<Staff>> modify(Staff staff){
        System.out.println("modify: "+staff.toString());

        //MD5加密 - 对修改的密码进行加密
//        String md5Password = DigestUtils.md5DigestAsHex(staff.getPassword().getBytes());
//        System.out.println(staff.getPassword() + ", " + md5Password);
//        staff.setPassword(md5Password);

        //BCrypt 加密
        staff.setPassword(CommonApi.encodePassword(staff.getPassword()));
        //更新数据库
        staffService.updateById(staff);
        //保存到 布隆过滤器
        bloomFilter.add(Integer.toString(staff.getId()));
        //保存到 redis
        redisUtils.set("staffCache::" + staff.getId(), staff);
        redisUtils.clearPageCache("staffPage");

        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    @GetMapping("/toMain")
    public String toMain(Admin admin, Model model){
        System.out.println(admin);
        model.addAttribute("admin", admin);
        return "main-staff";
    }

    //产品删除
    @GetMapping("/deleteSelected")
    @ResponseBody
    @CacheEvict(value = "staffCache", allEntries = true)  //清理 setmeal 缓存下的所有数据
    public LayuiUtils<List<Staff>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {

        //删除数据库+缓存
        staffService.deleteSelected(ids);
        redisUtils.clearPageCache("staffPage");

        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    @GetMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Staff staff;
        if (bloomFilter.mightContain(Integer.toString(id))) {
            System.out.println("布隆过滤器存在该id, 使用 redis 查询");
            staff = (Staff)redisUtils.get(redisCache, id);
        } else{
            staff = staffService.getById(id);
            //保存到 布隆过滤器
            bloomFilter.add(Integer.toString(staff.getId()));
            //保存到 redis
            redisUtils.set("staffCache::" + staff.getId(), staff);
        }

        //设置模型
        mv.addObject("staff", JSON.toJSONString(staff));
        System.out.println(JSON.toJSONString(staff));
        //设置视图
        mv.setViewName("staff-modify");
        return mv;
    }

    @GetMapping("/seeData/{id}")
    public ModelAndView seeData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Staff staff = staffService.getById(id);
        //设置模型
        mv.addObject("staff", JSON.toJSONString(staff));
        System.out.println(JSON.toJSONString(staff));
        //设置视图
        mv.setViewName("staff-see");
        return mv;
    }


    @GetMapping("/delete")
    @ResponseBody
    @CacheEvict(value = "staffCache", key = "#id", allEntries = true)  //清理 setmeal 缓存下的所有数据
    public LayuiUtils<List<Staff>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        // 从 MySQL 数据库中删除员工信息
        staffService.deleteSelected(id);
        //保存到 布隆过滤器
        bloomFilter.add(id);
        //删除缓存
        redisUtils.delete("staffCache::" + id);
        redisUtils.clearPageCache("staffPage");

        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    @GetMapping("/toAdd")
    public String toAdd(){
        return "staff-add";
    }

    @GetMapping("/toSignin")
    public String toSignin(){
        return "signin-staff";
    }


    @GetMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "staff-list";
    }


    //采用分页代码方法
    @GetMapping("/list")
    @ResponseBody
    @Cacheable(value = "staffPage", key="#page + '_' + #size", condition="#size == 10")
    public LayuiUtils<List<Staff>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        List<Staff> staffs = new ArrayList<Staff>();
        //缓存 - 如果 Redis 服务器不存在该缓存 - 存在问题
        boolean condition = true;
        Cache cache = null;
        try {
            cache = cacheManager.getCache("staffCache");
            condition = redisUtils.checkCacheExists("staffCache*");
            System.out.println(condition);
        } catch (RedisConnectionException e) {
            System.out.println("Redis 连接异常：" + e.getMessage());
            condition = false; // 设置条件为 false
        }
        if (!condition) {
            System.out.println("数据库");
            staffs = staffService.list();
            for (Staff staff : staffs) {
                cache.put(staff.getId(), staff);
                //保存到 布隆过滤器
                bloomFilter.add(Integer.toString(staff.getId()));
            }
        }
//        Cache cache = cacheManager.getCache("staffCache");
//        boolean condition = redisUtils.checkCacheExists("staffCache*");
//        System.out.println(condition);
//        if (!condition) {
//            System.out.println("数据库");
//            staffs = staffService.list();
//            for (Staff staff : staffs) {
//                cache.put(staff.getId(), staff);
//                //保存到 布隆过滤器
//                bloomFilter.add(Integer.toString(staff.getId()));
//            }
//        }

//        else {
//            System.out.println("缓存");
//            List<Object> objects = redisUtils.multiGet("staffPage::*");
//            staffs = new ArrayList<>();
//            for (Object value : objects) {
//                Staff staff = (Staff) value;
//                staffs.add(staff);
//            }
//        }

        //条件构造器对象
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(Staff::getName);
        //进行分页查询
        Page<Staff> pageinfo = new Page<Staff>(page, size);
        staffService.page(pageinfo, queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());
        System.out.println(pageinfo.toString());


        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @GetMapping("/search")
    @ResponseBody
    public LayuiUtils<List<Staff>> search(String name, String position,
                                          @RequestParam(name="page",required = true,defaultValue = "1")int page,
                                          @RequestParam(name="limit",required = true,defaultValue = "10")int size){
        System.out.println("search:"+name + position);
        Staff staff = new Staff();
        //设置参数
        staff.setName(name);
        staff.setPosition(position);
        LambdaQueryWrapper<Staff> lambdaQueryWrapper = new LambdaQueryWrapper<Staff>();
        lambdaQueryWrapper.like(name != null, Staff::getName,name);
        lambdaQueryWrapper.like(position != null, Staff::getPosition,position);
        //进行分页查询
        Page<Staff> pageinfo = new Page<Staff>(page, size);
        staffService.page(pageinfo, lambdaQueryWrapper);
        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }
}
