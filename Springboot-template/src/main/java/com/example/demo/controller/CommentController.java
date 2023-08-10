package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Comment;
import com.example.demo.services.CommentService;
import com.example.demo.utils.LayuiUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * CommentController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/comment")
@Controller
@Api(tags = "评论")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/save")
    @ResponseBody
    public LayuiUtils<List<Comment>> save(Comment object){
        System.out.println("save:"+object.toString());
        commentService.save(object);
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("1", null,1,0);
        return result;
    }

    @PutMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<Comment>> modify(Comment object){
        System.out.println("modify:"+object.toString());
        commentService.updateById(object);
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("1", null,1,0);
        return result;
    }

    /**
     * 产品删除
     * @param ids
     * @return
     * @throws Exception
     */
    @GetMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<Comment>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        commentService.deleteSelected(ids);
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("1", null,1,0);
        return result;
    }

    @GetMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Comment comment = commentService.getById(id);
        //设置模型
        mv.addObject("Comment", JSON.toJSONString(comment));
        System.out.println(JSON.toJSONString(comment));
        //设置视图
        mv.setViewName("comment-modify");
        return mv;
    }

    @GetMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<Comment>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        commentService.deleteSelected(id);
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("1", null,1,0);
        return result;
    }

    @GetMapping("/toAdd")
    public String toAdd(){
        return "comment-add";
    }


    @GetMapping("/toList")
    public String toList(){
        return "comment-list";
    }

    /**
     * 采用分页代码方法
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public LayuiUtils<List<Comment>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(Comment::getName);
        //进行分页查询
        Page<Comment> pageinfo = new Page<Comment>(page, size);
        commentService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @GetMapping("/search")
    @ResponseBody
    public LayuiUtils<List<Comment>> search(String name, String position){
        System.out.println("search:"+name + position);
        Comment comment = new Comment();
        //设置参数
        comment.setName(name);
        LambdaQueryWrapper<Comment> lambdaQueryWrapper = new LambdaQueryWrapper<Comment>();
        lambdaQueryWrapper.like(name != null, Comment::getName,name);
        List<Comment> commentList = commentService.list(lambdaQueryWrapper);
        //使用PageInfo包装数据
        PageInfo<Comment> pageInfo = new PageInfo<Comment> (commentList,3);
        //打印封装数据
        LayuiUtils<List<Comment>> result = new LayuiUtils<List<Comment>>("", commentList,0,(int)pageInfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }

}
