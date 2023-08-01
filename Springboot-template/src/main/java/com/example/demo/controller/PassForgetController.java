package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.PassForget;
import com.example.demo.bean.Staff;
import com.example.demo.bean.User;
import com.example.demo.services.PassForgetService;
import com.example.demo.services.StaffService;
import com.example.demo.services.UserService;
import com.example.demo.utils.CommonApi;
import com.example.demo.utils.LayuiUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * PassForgetController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/passforget")
@Controller
public class PassForgetController {
    @Autowired
    private PassForgetService passforgetService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private UserService userService;

    @RequestMapping("/save")
    @ResponseBody
    public LayuiUtils<List<PassForget>> save(PassForget object){
        System.out.println("save:"+object.toString());
        passforgetService.save(object);
        //打印封装数据
        return new LayuiUtils<List<PassForget>>("1", null,1,0);
    }

    @RequestMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<PassForget>> modify(PassForget object){
        System.out.println("modify:"+object.toString());
        passforgetService.updateById(object);
        //打印封装数据
        return new LayuiUtils<List<PassForget>>("1", null,1,0);
    }

    @RequestMapping("/agree")
    @ResponseBody
    public LayuiUtils<PassForget> agree(String id){
        System.out.println("agree:" + id);
        PassForget passForget = passforgetService.getById(id);
        //根据验证的 type 类型进行修改密码
        //1、职员
        if(passForget.getType() == 0){
            //查询 staff 表
            Staff staff = staffService.getById(passForget.getUid());
            //修改密码
            //BCrypt 加密
            staff.setPassword(CommonApi.encodePassword(staff.getPassword()));
            //更新数据库
            staffService.updateById(staff);
        }
        //2、用户
        else if(passForget.getType() == 1){
            //查询 User  表
            User user = userService.getById(passForget.getUid());
            //修改密码
            //BCrypt 加密
            user.setPassword(CommonApi.encodePassword(user.getPassword()));
            //更新数据库
            userService.updateById(user);
        }
        //修改成功 - 删除请求数据
        passforgetService.removeById(id);
        //打印封装数据
        return new LayuiUtils<PassForget>("修改成功", null,0,0);
    }


    /**
     * 产品删除
     * @param ids
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<PassForget>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        passforgetService.deleteSelected(ids);
        //打印封装数据
        return new LayuiUtils<List<PassForget>>("1", null,1,0);
    }

    @RequestMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        System.out.println("agree:" + id);
        PassForget passForget = passforgetService.getById(id);
        //根据验证的 type 类型进行回调
        //1、职员
        User user = new User();
        if(passForget.getType() == 0){
            //查询 staff 表
            Staff staff = staffService.getById(id);
            //复制 Staff
            BeanUtils.copyProperties(staff, user);
        }
        //2、用户
        else if(passForget.getType() == 1){
            //查询 User  表
            user = userService.getById(id);
            //修改密码
            //BCrypt 加密
            user.setPassword(CommonApi.encodePassword(user.getPassword()));
            //更新数据库
            userService.updateById(user);
        }
        System.out.println(user.toString());
        //设置模型
        ModelAndView mv = new ModelAndView();
        mv.addObject("user", JSON.toJSONString(user));
        //设置视图
        mv.setViewName("passForget-see");
        return mv;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<PassForget>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        passforgetService.deleteSelected(id);
        //打印封装数据
        return new LayuiUtils<List<PassForget>>("1", null,1,0);
    }

    @RequestMapping("/toAdd")
    public String toAdd(){
        return "passForget-add";
    }


    @RequestMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "passForget-list";
    }

    /**
     * 采用分页代码方法
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public LayuiUtils<List<PassForget>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<PassForget> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(PassForget::getId);
        //进行分页查询
        Page<PassForget> pageinfo = new Page<>(page, size);
        passforgetService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        return new LayuiUtils<List<PassForget>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
    }
}
