package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.News;
import com.example.demo.bean.Staff;
import com.example.demo.services.NewsService;
import com.example.demo.utils.LayuiUtils;
import com.github.pagehelper.PageInfo;
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
 * NewsController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/news")
@Controller
public class NewsController {
    @Autowired
    private NewsService newsService;

    @RequestMapping("/save")
    @ResponseBody
    public LayuiUtils<List<Staff>> save(News object){
        System.out.println("save:"+object.toString());
        newsService.save(object);
        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }
    

    @RequestMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<Staff>> modify(News staff){
        System.out.println("modify:"+staff.toString());
        newsService .updateById(staff);
        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    //产品删除
    @RequestMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<Staff>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        newsService .deleteSelected(ids);
        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    @RequestMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        News news = newsService .getById(id);
        //设置模型
        mv.addObject("news", JSON.toJSONString(news));
        System.out.println(JSON.toJSONString(news));
        //设置视图
        mv.setViewName("news-modify");
        return mv;
    }


    @RequestMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<Staff>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        newsService .deleteSelected(id);
        //打印封装数据
        LayuiUtils<List<Staff>> result = new LayuiUtils<List<Staff>>("1", null,1,0);
        return result;
    }

    @RequestMapping("/toAdd")
    public String toAdd(){
        return "news-add";
    }

    @RequestMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "news-list";
    }

    //采用分页代码方法
    @RequestMapping("/list")
    @ResponseBody
    public LayuiUtils<List<News>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(News::getTitle);
        //进行分页查询
        Page<News> pageinfo = new Page<News>(page, size);
        newsService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<News>> result = new LayuiUtils<List<News>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    //采用分页代码方法
    @RequestMapping("/list_news")
    @ResponseBody
    public LayuiUtils<List<News>> list_news(@RequestParam(name="page",required = true,defaultValue = "1")int page, @RequestParam(name="limit",required = true,defaultValue = "3")int limit) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<News>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(News::getTitle);
        //进行分页查询
        Page<News> pageinfo = new Page<News>(page, limit);
        newsService.page(pageinfo, queryWrapper);
        System.out.println(page + "  " + limit);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        mv.addObject("news", pageinfo.getRecords());
        //打印封装数据
        LayuiUtils<List<News>> result = new LayuiUtils<List<News>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @RequestMapping("/search")
    @ResponseBody
    public LayuiUtils<List<News>> search(String name, String position){
        System.out.println("search:"+name + position);
        News news = new News();
        //设置参数
        news.setTitle(name);
        LambdaQueryWrapper<News> lambdaQueryWrapper = new LambdaQueryWrapper<News>();
        lambdaQueryWrapper.like(name != null, News::getTitle, name);
        List<News> newsList = newsService .list(lambdaQueryWrapper);
        //使用PageInfo包装数据
        PageInfo<News> pageInfo = new PageInfo<News> (newsList,3);
        //打印封装数据
        LayuiUtils<List<News>> result = new LayuiUtils<List<News>>("", newsList,0,(int)pageInfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }
}
