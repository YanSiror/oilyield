package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Student;
import com.example.demo.bean.Well;
import com.example.demo.bean.WellE;
import com.example.demo.mapper.WellEMapper;
import com.example.demo.mapper.WellMapper;
import com.example.demo.services.WellService;
import com.example.demo.utils.LayuiUtils;
import com.github.pagehelper.PageInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在该文件集成了 Redis 缓存
 */

@RequestMapping("/well")
@Controller
public class WellController {
    @Autowired
    private WellService wellService;
    @Autowired
    private WellEMapper wellEMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @RequestMapping("/save")
    @ResponseBody
    public LayuiUtils<List<Well>> save(Well object){
        System.out.println("save:"+object.toString());
        wellService.save(object);
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("1", null,1,0);
        return result;
    }

    @RequestMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<Well>> modify(Well Well){
        System.out.println("modify:"+Well.toString());
        wellService.updateById(Well);
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("1", null,1,0);
        return result;
    }

    //产品删除
    @RequestMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<Well>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        wellService.deleteSelected(ids);
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("1", null,1,0);
        return result;
    }

    @RequestMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Well Well = wellService.getById(id);
        //设置模型
        mv.addObject("well", JSON.toJSONString(Well));
        System.out.println(JSON.toJSONString(Well));
        //设置视图
        mv.setViewName("well-modify");
        return mv;
    }

    @RequestMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<Well>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        wellService.deleteSelected(id);
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("1", null,1,0);
        return result;
    }

    @RequestMapping("/toAdd")
    public String toAdd(){
        return "well-add";
    }


    @RequestMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "well-list";
    }

    //采用分页代码方法
    @RequestMapping("/list")
    @ResponseBody
    public LayuiUtils<List<Well>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "15")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<Well> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(Well::getName);
        //进行分页查询
        Page<Well> pageinfo = new Page<Well>(page, size);
        wellService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @RequestMapping("/search")
    @ResponseBody
    public LayuiUtils<List<Well>> search(String name, String position){
        System.out.println("search:"+name + position);

        //准备将所有的单井信息保存到 elasticSearch 用于分词提示
        List<Well> wells = wellService.list();
        List<WellE> wellEs = new ArrayList<>();
        for(Well well : wells){
            wellEs.add(new WellE(well));
        }
        wellEs.forEach(well -> {
            List<String> suggestList = Collections.singletonList(well.getName());
            well.setSuggest(new Completion(suggestList.toArray(new String[0])));
        });
        wellEMapper.saveAll(wellEs);

        //实际的查找逻辑
        Well well = new Well();
        //设置参数
        well.setName(name);
        LambdaQueryWrapper<Well> lambdaQueryWrapper = new LambdaQueryWrapper<Well>();
        lambdaQueryWrapper.like(name != null, Well::getName,name);
        List<Well> welllist = wellService.list(lambdaQueryWrapper);
        //使用PageInfo包装数据
        PageInfo<Well> pageInfo = new PageInfo<Well> (welllist,3);
        //打印封装数据
        LayuiUtils<List<Well>> result = new LayuiUtils<List<Well>>("", welllist,0,(int)pageInfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }

    //采用分页代码方法
    @RequestMapping("/suggest")
    @ResponseBody
    public LayuiUtils<List<String>> suggest(String keyword) {
        // 使用suggest进行标题联想
        CompletionSuggestionBuilder suggest = SuggestBuilders.completionSuggestion("suggest")
                // 关键字（参数传此）
                .prefix(keyword)
                // 重复过滤
                .skipDuplicates(true)
                // 匹配数量
                .size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-suggest", suggest);

        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(WellE.class);
        // 查询
        SearchResponse wellNameSuggestResp = elasticsearchRestTemplate.suggest(suggestBuilder, indexCoordinates);
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> wellNameSuggest
                = wellNameSuggestResp.getSuggest().getSuggestion("my-suggest");

        // 处理返回
        List<String> suggests = wellNameSuggest.getEntries().stream().map(
                x -> x.getOptions().stream().map(
                y -> y.getText().toString()).collect(Collectors.toList())).findFirst().get();
        // 输出内容
//        for (String su : suggests) {
//            System.out.println("suggest = " + su);
//        }
        System.out.println("\033[32m" + "Keyword = " + keyword + "向 Elastic 发送了一次 suggest 请求..." +"\033[0m");

        //打印封装数据
        LayuiUtils<List<String>> result = new LayuiUtils<List<String>>("", suggests,0,suggests.size());
        return result;
    }
}
