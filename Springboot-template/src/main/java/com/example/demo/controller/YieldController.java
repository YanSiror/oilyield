package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.*;
import com.example.demo.mapper.WellEMapper;
import com.example.demo.services.WellService;
import com.example.demo.services.YieldService;
import com.example.demo.utils.DateUtils;
import com.example.demo.utils.LayuiUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jni.Time;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在该文件集成了 Redis 缓存
 */

@RequestMapping("/yield")
@Controller
@Api(tags = "产量")
public class YieldController {
    @Autowired
    private YieldService yieldService;
    @Autowired
    private WellService wellService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    RedisTemplate<String, Object> redisTest;
    @Autowired
    private WellEMapper wellEMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public boolean checkYieldCacheExists(String name) {
        return redisTemplate.hasKey(name);
    }

    @PostMapping("/save")
    @ResponseBody
    public LayuiUtils<List<Yield>> save(Yield object){
        System.out.println("save:"+object.toString());
        yieldService.save(object);
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("1", null,1,0);
        return result;
    }

    @PutMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<Yield>> modify(Yield yield){
        System.out.println("modify:"+yield.toString());
        yieldService.updateById(yield);
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("1", null,1,0);
        return result;
    }

    //产品删除
    @GetMapping("/deleteSelected")
    @ResponseBody
    public LayuiUtils<List<Yield>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        yieldService.deleteSelected(ids);
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("1", null,1,0);
        return result;
    }

    @GetMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        System.out.println(id);
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Yield yield = yieldService.getById(id);
        yield.setStatus(0);

        //设置模型
        mv.addObject("yield", JSON.toJSONString(yield));
        System.out.println(JSON.toJSONString(yield));
        //设置视图
        mv.setViewName("yield-modify");
        return mv;
    }

    @GetMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<Yield>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        yieldService.deleteSelected(id);
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("1", null,1,0);
        return result;
    }

    @GetMapping("/toAdd")
    public String toAdd(){
        return "yield-add";
    }

    @GetMapping("/toImport")
    public String toImport(){
        return "yield-import";
    }


    @GetMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "yield-list";
    }

    //采用分页代码方法
    @GetMapping("/list")
    @ResponseBody
    public LayuiUtils<List<Yield>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,
                                        @RequestParam(name="limit",required = true,defaultValue = "15")int size,
                                        @RequestParam(name="limitValue",required = true,defaultValue = "10")double limitValue) {
        System.out.println("-----------------------" + limitValue);
//        //缓存 - 如果 Redis 服务器不存在该缓存
//        Cache cache = cacheManager.getCache("yieldCache");
//        Set<String> cacheNames = (Set<String>) cacheManager.getCacheNames();
//        boolean exists = cacheNames.contains("yieldCache");
//        System.out.println(!exists);
//        System.out.println(!checkYieldCacheExists("yieldCache::1"));
//        if (!checkYieldCacheExists("yieldCache::1")) {
//            List<Yield> yields = yieldService.list();
//            for (Yield yield : yields) {
//                cache.put(yield.getId(), yield);
//            }
//        }

        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<Yield> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.ge(Yield::getId,0);
        //进行分页查询
        Page<Yield> pageinfo = new Page<Yield>(page, size);
        yieldService.page(pageinfo, queryWrapper);

        //添加预警字段
        double limit = 10;
        limit = limitValue;
        for (Yield yield: pageinfo.getRecords()) {
            //由于数据过多, 少部分展示正常数据
            if(yield.getGas() == null){
                yield.setGas(0.0);
            }
            if(yield.getOil() == null){
                yield.setOil(0.0);
            }
            System.out.println(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid());
            if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() < limit*0.2 && yield.getPtime() > 0){
                yield.setStatus(0);
            }
            else if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() > limit*1.8){
                yield.setStatus(0);
            }
            else{
                yield.setStatus(1);
            }
        }

        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());
        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }


    //采用分页代码方法
    @PostMapping("/listLimit")
    @ResponseBody
    public LayuiUtils<List<Yield>> listLimit(String name, String startDate,  String endDate, @RequestParam(name="page",required = true,defaultValue = "1")int page,
                                        @RequestParam(name="limit",required = true,defaultValue = "15")int size,
                                        @RequestParam(name="limitValue",required = true,defaultValue = "10")double limitValue) throws ParseException {
        System.out.println("-----------------------" + limitValue);
        System.out.println(name + startDate + ", " +endDate);


        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        //设置参数
        Date startYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDate);
        Date endYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDate);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWname, name)
                .between(Yield::getTime, startYear, endYear)
                .ge(Yield::getId,0);
        //进行分页查询
        Page<Yield> pageinfo = new Page<Yield>(page, size);
        yieldService.page(pageinfo, lambdaQueryWrapper);

        //添加预警字段
        double limit;
        limit = limitValue;
        for (Yield yield: pageinfo.getRecords()) {
            //由于数据过多, 少部分展示正常数据
            if(yield.getGas() == null){
                yield.setGas(0.0);
            }
            if(yield.getOil() == null){
                yield.setOil(0.0);
            }
            System.out.println(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid());
            if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() < limit*0.2 && yield.getPtime() > 0){
                yield.setStatus(0);
            }
            else if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() > limit*1.8){
                yield.setStatus(0);
            }
            else{
                yield.setStatus(1);
            }
        }

        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    //采用分页代码方法
    @GetMapping("/listWarn")
    public String listWarn(Model model) {
        //打印封装数据
        return "yield-warn";
    }

    //采用分页代码方法
    @GetMapping("/listAll")
    public String listAll(Model model) {
        List<Well> wells = wellService.list();
        model.addAttribute("wells", wells);
        //打印封装数据
        return "yield-show";
    }

    @PostMapping("/showPie")
    @ResponseBody
    public Map<String, Object> showPie(String name, String time, Model model) throws ParseException {
        System.out.println(name + " " + time);
        //根据名称和日期查询
        Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWname, name)
                .ge(Yield::getId,0)
                .apply("DATE_FORMAT(time, '%Y-%m-%d') = '" + new SimpleDateFormat("yyyy-MM-dd").format(startTime) + "'");
        List<Yield> yields = yieldService.list(lambdaQueryWrapper);
        Map<String, Object> map = new HashMap<>();
        if(yields.size() > 0){
            Yield yield = yields.get(0);
            List<String> xdata = Arrays.asList("产液", "产油", "产水", "产气");
            List<Double> ydata = Arrays.asList(yield.getLiquid(), yield.getWater(), yield.getGas(), yield.getGas());
            map.put("xdata", xdata);
            map.put("ydata", ydata);
            map.put("code", 0);
            System.out.println(xdata);
            System.out.println(ydata);
        } else{
            map.put("code", 1);
        }
        return map;
    }


    @PostMapping("/showWarn")
    @ResponseBody
    public Map<String, Object> showWarn(double limit, Model model) throws ParseException, JsonProcessingException {
        System.out.println(limit);
        LambdaQueryWrapper<Yield> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.last("limit 100");
        List<Yield> yields = yieldService.list(queryWrapper);

        //异常数据检测， 读取数据并返回
        //设置阈值 limit 为警戒线, 预警
        //所有产出为 0 且生产时间 > 0, 预警; 其它不预警
        List<Integer> warns = new ArrayList<>();
        List<String> names = new ArrayList<>();
        int i = 1;
        for (Yield yield : yields) {
            //由于数据过多, 少部分展示正常数据
            if(yield.getGas() == null){
                yield.setGas(0.0);
            }
            if(yield.getOil() == null){
                yield.setOil(0.0);
            }
            //截断为200
            if(i == 200){
                break;
            }
            if(!(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() < limit*0.8 && yield.getPtime() > 0) && i % 100 == 0){
                continue;
            }

            names.add(yield.getWname()+ " : " + DateUtils.dateToStringNoTime(yield.getTime()));
            if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() < limit*0.8 && yield.getPtime() > 0){
                warns.add(-1);
            }
            else if(yield.getGas() + yield.getOil() + yield.getWater() + yield.getLiquid() > limit*1.2){
                warns.add(-1);
            }
            else{
                warns.add(1);
            }
            i++;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("warns", warns);
        map.put("names", names);
        //System.out.println(yields.toString());
        model.addAttribute("yields", yields);
        return map;
    }

    //采用分页代码方法
    @GetMapping("/listTrend")
    public String listTrend(Model model) {
        List<Well> wells = wellService.list();
        model.addAttribute("wells", wells);
        List<String> xdata = Arrays.asList("产液", "产油", "产水", "产气", "生产时间");
        List<Integer> ydata = Arrays.asList(20, 40, 60, 80, 100);
        model.addAttribute("xdata", xdata);
        model.addAttribute("ydata", ydata);
        //打印封装数据
        return "yield-trend";
    }

    @PostMapping("/showLine")
    @ResponseBody
    public Map<String, Object> showLine(String startTime, String endTime, String name, Model model) throws ParseException {
        System.out.println(startTime + "," + endTime + ", " +name);

        Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Date end = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWid, Integer.parseInt(name))
                .between(Yield::getTime, start, end);
        List<Yield> yields = yieldService.list(lambdaQueryWrapper);

        List<Double> liquids = new ArrayList<>();
        List<Double> waters = new ArrayList<>();
        List<Double> gases = new ArrayList<>();
        List<Double> oils = new ArrayList<>();
        List<Double> rates = new ArrayList<>();
        List<Double> totals = new ArrayList<>();
        List<String> times = new ArrayList<>();
        double max = 0;
        for (Yield y: yields) {
            if(y.getGas() == null){
                y.setGas(0.0);
            }
            if(y.getOil() == null){
                y.setOil(0.0);
            }
            if(y.getRate() == null){
                y.setRate(0.0);
            }
            double t = y.getLiquid() + y.getWater() + y.getGas() + y.getOil();
            //求得最大区间
            max = Math.max(max, y.getLiquid());
            max = Math.max(max, y.getWater());
            max = Math.max(max, y.getGas());
            max = Math.max(max, y.getOil());

            //剔除没有产量数据的数据
            if(t == 0){
                continue;
            }
            liquids.add(y.getLiquid());
            waters.add(y.getWater());
            gases.add(y.getGas());
            oils.add(y.getOil());
            rates.add(y.getRate() / 100);
            totals.add(t);
            times.add(DateUtils.dateToString(y.getTime()));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("liquids", liquids);
        map.put("waters", waters);
        map.put("gases", gases);
        map.put("oils", oils);
        map.put("rates", rates);
        map.put("totals", totals);
        map.put("times", times);
        map.put("max", (int)max);
        //System.out.println(yields.toString());
        model.addAttribute("yields", yields);
        return map;
    }

    //采用分页代码方法
    @GetMapping("/listAnalyse")
    public String listAnalyse(Model model) {
        List<Well> wells = wellService.list();
        //System.out.println(yields.toString());
        model.addAttribute("wells", wells);
        //打印封装数据
        return "yield-analyse";
    }

    //采用分页代码方法
    @GetMapping("/listGroup")
    public String listGroup(Model model) {
        List<Integer> groups = new ArrayList<>();
        Cache cache = cacheManager.getCache("wellgroup");
        Set<String> cacheNames = (Set<String>) cacheManager.getCacheNames();
        boolean exists = cacheNames.contains("wellgroup");
        System.out.println(exists);
        System.out.println(checkYieldCacheExists("wellgroup::1"));
        if (!checkYieldCacheExists("wellgroup::1")) {
            LambdaQueryWrapper<Yield> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Yield::getWid);         //只返回 wid 字段, 大大执行时间
            List<Yield> yields = yieldService.list(queryWrapper);
            //根据 yields 获取所有的组别
            Set<Integer> widSet = new HashSet<>();
            for (Yield yield: yields) {
                widSet.add(yield.getWid());
            }
            groups = new ArrayList<>(widSet);
            cache.put("1", groups);
        } else{
            groups = (List<Integer>) redisTemplate.opsForValue().get("wellgroup::1");
            System.out.println("缓存: " + groups.toString());
        }
        model.addAttribute("groups", groups);
        //打印封装数据
        return "yield-group";
    }

    public void parseNumerList(double[] x){
        DecimalFormat df = new DecimalFormat("#.###"); // 定义格式，保留三位小数
        for (int i = 0; i < x.length; i++) {
            x[i] = Double.parseDouble(df.format(x[i]));
        }
    }

    @PostMapping("/showGroup")
    @ResponseBody
    public Map<String, Object> showGroup(String grouped, String year_choose, String type, String startTime, String endTime, Model model) throws ParseException {
        System.out.println(grouped + "," + year_choose + ", " +type + ", " +startTime + ", " +endTime);

        Date startYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Date endYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWid, grouped)
                .between(Yield::getTime, startYear, endYear);
        List<Yield> yieldList = yieldService.list(lambdaQueryWrapper);
        //根据wid划分为不同组
        Map<String, List<Yield>> yieldGroups = yieldList.stream()
                .collect(Collectors.groupingBy(Yield::getWname));

        Map<String, Object> re_maps = new HashMap<>();
        List<String> xais = new ArrayList<>();
        if("月".equals(year_choose)){
            xais.add(Integer.toString(getYearFromDate(startYear)));
        } else if("年".equals(year_choose)){
            for (int year = getYearFromDate(startYear); year < getYearFromDate(endYear); year++){
                xais.add(Integer.toString(year));
            }
        }

        int count = -1;
        if("月".equals(year_choose)){
            count = 1;
        } else if("年".equals(year_choose)){
            count = getYearFromDate(endYear) - getYearFromDate(startYear) + 1;
        }

        System.out.println("print: " + count);

        int c_ = yieldGroups.size();
        List<String> xnames = new ArrayList<>();
        List<double []> returns = new ArrayList<>();
        int finalCount = count;
        int max = 0;
        yieldGroups.forEach((wname, yields) -> {
            System.out.println("----------------------\nwname: " + wname);
            //yields.forEach(yield -> System.out.println(yield.toString()));

            // 将 yields 按照 time 字段升序排序
            //Collections.sort(yields, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));
            yields.sort(Comparator.comparing(Yield::getTime));
            xnames.add(wname);


            // 使用 TreeMap 对处理后的数据进行按年份划分和统计，其中 key 是年份
            Map<Integer, List<Yield>> yearlyDataMap = new TreeMap<>();
            Calendar calendar = Calendar.getInstance(); // 用于操作日期
            Calendar startYearCalendar = Calendar.getInstance();
            startYearCalendar.setTime(startYear);
            Calendar endYearCalendar = Calendar.getInstance();
            endYearCalendar.setTime(endYear);

            // 遍历所有 yield 数据
            for (Yield yield : yields) {
                int year = getYearFromDate(yield.getTime());
                if (year >= getYearFromDate(startYear) && year <= getYearFromDate(endYear)) {
                    // 如果该 yield 数据的年份位于起始年份和结束年份之间，就将其加入对应的年份列表中
                    if (!yearlyDataMap.containsKey(year)) {
                        yearlyDataMap.put(year, new ArrayList<>());
                    }
                    yearlyDataMap.get(year).add(yield);
                }
            }
            double[] liquids_year = new double[finalCount];
            double[] waters_year = new double[finalCount];
            double[] oils_year = new double[finalCount];
            double[] rates_year = new double[finalCount];
            double[] totals_year = new double[finalCount];
            double[] gases_year = new double[finalCount];
            // 按年份进行遍历
            int tag = 0;
            for (int year = getYearFromDate(startYear); year <= getYearFromDate(endYear); year++) {
                double []sums = new double[6];

                // 如果当前年份在输入数据的时间范围内，则按月份进行排序并输出
                if (yearlyDataMap.containsKey(year)) {
                    List<Yield> yearlyData = yearlyDataMap.get(year);
                    Collections.sort(yearlyData, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));

                    System.out.println("年份：" + year);
                    int currentMonth = -1;
                    for (Yield yield : yearlyData) {
                        // 按月份输出 yield 数据，跳过相同月份的数据
                        calendar.setTime(yield.getTime());
                        int month = calendar.get(Calendar.MONTH);
                        if (month != currentMonth) {
                            double liquidSum = 0.0; // 液体总产量
                            double oilSum = 0.0; // 原油总产量
                            double waterSum = 0.0; // 水总产量
                            double gasSum = 0.0; // 气总产量
                            double rateSum = 0.0; // 气总产量
                            double totalSum; // 气总产量

                            // 计算本月份的产量总量
                            for (Yield monthlyYield : yearlyData) {
                                calendar.setTime(monthlyYield.getTime());
                                if (calendar.get(Calendar.MONTH) == month) {
                                    liquidSum += monthlyYield.getLiquid(); // 累计液体总产量
                                    oilSum += monthlyYield.getOil(); // 累计原油总产量
                                    waterSum += monthlyYield.getWater(); // 累计水总产量
                                    if(monthlyYield.getGas() != null){
                                        gasSum += monthlyYield.getGas(); // 累计气总产量
                                    }
                                    if(monthlyYield.getRate() != null){
                                        rateSum += monthlyYield.getRate(); // 累计气总产量
                                    }
                                }
                            }
                            totalSum = liquidSum + oilSum + waterSum + gasSum; // 累计气总产量
                            liquids_year[year - getYearFromDate(startYear)] += liquidSum;
                            waters_year[year - getYearFromDate(startYear)] += waterSum;
                            oils_year[year - getYearFromDate(startYear)] += oilSum;
                            rates_year[year - getYearFromDate(startYear)] += rateSum;
                            totals_year[year - getYearFromDate(startYear)] += totalSum;
                            gases_year[year - getYearFromDate(startYear)] += gasSum;

                            // 输出当前月份的产量信息
                            System.out.println("    " + formatNumber(month + 1) + "月份:\t" +
                                    "液体产量:" + liquidSum + "\t" +
                                    "原油产量:" + oilSum + "\t" +
                                    "水产量:" + waterSum + "\t" +
                                    "含水率:" + rateSum + "\t" +
                                    "总产量:" + totalSum + "\t" +
                                    "气产量:" + gasSum);
                            currentMonth = month;
                        }
                    }
                    rates_year[year - getYearFromDate(startYear)] = rates_year[year - getYearFromDate(startYear)] / 12;
                    System.out.println("    " + formatNumber(year) + "年份:\t" +
                            "液体产量:" + liquids_year[year - getYearFromDate(startYear)] + "\t" +
                            "原油产量:" + oils_year[year - getYearFromDate(startYear)] + "\t" +
                            "水产量:" + waters_year[year - getYearFromDate(startYear)] + "\t" +
                            "含水率:" + rates_year[year - getYearFromDate(startYear)] + "\t" +
                            "总产量:" + totals_year[year - getYearFromDate(startYear)] + "\t" +
                            "气产量:" + gases_year[year - getYearFromDate(startYear)] );
                }
                tag += 1;
                if("月".equals(year_choose) && tag == 1){
                    break;
                }
            }
            // 保留2位小数
            parseNumerList(liquids_year);
            parseNumerList(oils_year);
            parseNumerList(waters_year);
            parseNumerList(gases_year);
            parseNumerList(rates_year);
            parseNumerList(totals_year);
            if("产油".equals(type)){
                returns.add(oils_year);
            } else if ("产水".equals(type)){
                returns.add(waters_year);
            }
            else if ("产气".equals(type)){
                returns.add(gases_year);
            }
            else if ("产液".equals(type)){
                returns.add(liquids_year);
            }
            else if ("含水".equals(type)){
                returns.add(rates_year);
            }
            else if ("总计".equals(type)){
                returns.add(totals_year);
            }
            System.out.println(xais);
            System.out.println(Arrays.toString(liquids_year));
            System.out.println(Arrays.toString(waters_year));
            System.out.println(Arrays.toString(oils_year));
            System.out.println(Arrays.toString(rates_year));
            System.out.println(Arrays.toString(totals_year));
            System.out.println(Arrays.toString(gases_year));
        });

        System.out.println("returns: ");
        System.out.println(returns);
        //转化 returns
        for(int i = 0; i < returns.size();i++){
            System.out.println(Arrays.toString(returns.get(i)));
            re_maps.put(xnames.get(i), returns.get(i));
            //计算最大值, 用于设置 max_scale
            // 找到新数组中的最大值
            double maxValue = Double.MIN_VALUE;
            for (double value : returns.get(i)) {
                if (value > maxValue) {
                    maxValue = value;
                }
            }
            // 将最大值转换为整数并返回
            if((int)maxValue > max){
                max = (int)maxValue;
            }
        }
        System.out.println(xais);
        System.out.println(xnames);
        re_maps.put("xais", xais);
        re_maps.put("xnames", xnames);
        re_maps.put("max", max);
        return re_maps;
    }

    /**
     * 从 Date 类型的对象中获取年份
     */
    private static int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 将单个数字格式化为两位数
     */
    private static String formatNumber(int number) {
        return String.format("%02d", number);
    }

    @PostMapping("/showAnalyse")
    @ResponseBody
    public Map<String, Object> showAnalyse(String name, String year_choose, String type, String startTime, String endTime, Model model) throws ParseException {
        System.out.println(name + "," + year_choose + ", " +type + ", " +startTime + ", " +endTime);
        Date startYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Date endYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWname, name)
                .between(Yield::getTime, startYear, endYear);
        List<Yield> yields = yieldService.list(lambdaQueryWrapper);
        // 将 yields 按照 time 字段升序排序
        Collections.sort(yields, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));
        // 使用 TreeMap 对处理后的数据进行按年份划分和统计，其中 key 是年份
        Map<Integer, List<Yield>> yearlyDataMap = new TreeMap<>();
        Calendar calendar = Calendar.getInstance(); // 用于操作日期
        Calendar startYearCalendar = Calendar.getInstance();
        startYearCalendar.setTime(startYear);
        Calendar endYearCalendar = Calendar.getInstance();
        endYearCalendar.setTime(endYear);
        int max = 0;

        // 遍历所有 yield 数据
        for (Yield yield : yields) {
            int year = getYearFromDate(yield.getTime());
            if (year >= getYearFromDate(startYear) && year <= getYearFromDate(endYear)) {
                // 如果该 yield 数据的年份位于起始年份和结束年份之间，就将其加入对应的年份列表中
                if (!yearlyDataMap.containsKey(year)) {
                    yearlyDataMap.put(year, new ArrayList<>());
                }
                yearlyDataMap.get(year).add(yield);
            }
        }

        List<String> xais = new ArrayList<>();
        List<Map<String, Object>> return_lists = new ArrayList<>();
        Map<String, Object> re_maps = new HashMap<>();
        // 按年份进行遍历
        int tag = 0;
        for (int year = getYearFromDate(startYear); year <= getYearFromDate(endYear); year++) {
            // 如果当前年份在输入数据的时间范围内，则按月份进行排序并输出
            if (yearlyDataMap.containsKey(year)) {
                List<Yield> yearlyData = yearlyDataMap.get(year);
                Collections.sort(yearlyData, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));

                System.out.println("年份：" + year);
                xais.add(Integer.toString(year));
                int currentMonth = -1;

                double[] liquids = new double[12];
                double[] waters = new double[12];
                double[] oils = new double[12];
                double[] rates = new double[12];
                double[] totals = new double[12];
                double[] gases = new double[12];
                for (Yield yield : yearlyData) {
                    // 按月份输出 yield 数据，跳过相同月份的数据
                    calendar.setTime(yield.getTime());
                    int month = calendar.get(Calendar.MONTH);
                    if (month != currentMonth) {
                        double liquidSum = 0.0; // 液体总产量
                        double oilSum = 0.0; // 原油总产量
                        double waterSum = 0.0; // 水总产量
                        double gasSum = 0.0; // 气总产量
                        double rateSum = 0.0; // 气总产量
                        double totalSum = 0.0; // 气总产量

                        // 计算本月份的产量总量
                        for (Yield monthlyYield : yearlyData) {
                            calendar.setTime(monthlyYield.getTime());
                            if (calendar.get(Calendar.MONTH) == month) {
                                liquidSum += monthlyYield.getLiquid(); // 累计液体总产量
                                oilSum += monthlyYield.getOil(); // 累计原油总产量
                                waterSum += monthlyYield.getWater(); // 累计水总产量
                                if(monthlyYield.getGas() != null){
                                    gasSum += monthlyYield.getGas(); // 累计气总产量
                                }
                                if(monthlyYield.getRate() != null){
                                    rateSum += monthlyYield.getRate(); // 累计气总产量
                                }
                            }
                        }
                        totalSum = liquidSum + oilSum + waterSum + gasSum; // 累计气总产量


                        liquids[month] = liquidSum;
                        oils[month] = oilSum;
                        waters[month] = waterSum;
                        gases[month] = gasSum;
                        rates[month] = rateSum;
                        totals[month] = totalSum;
                        // 输出当前月份的产量信息
                        System.out.println("    " + formatNumber(month + 1) + "月份:\t" +
                                "液体产量:" + liquidSum + "\t" +
                                "原油产量:" + oilSum + "\t" +
                                "水产量:" + waterSum + "\t" +
                                "含水率:" + rateSum + "\t" +
                                "总产量:" + totalSum + "\t" +
                                "气产量:" + gasSum);
                        currentMonth = month;
                    }
                }
                //计算最大值, 用于设置 max_scale
                // 将四个数组合并为一个新的数组
                double[] allValues = new double[liquids.length + waters.length + oils.length + gases.length];
                System.arraycopy(liquids, 0, allValues, 0, liquids.length);
                System.arraycopy(waters, 0, allValues, liquids.length, waters.length);
                System.arraycopy(oils, 0, allValues, liquids.length + waters.length, oils.length);
                System.arraycopy(gases, 0, allValues, liquids.length + waters.length + oils.length, gases.length);

                // 找到新数组中的最大值
                double maxValue = Double.MIN_VALUE;
                for (double value : allValues) {
                    if (value > maxValue) {
                        maxValue = value;
                    }
                }
                // 将最大值转换为整数并返回
                if((int)maxValue > max){
                    max = (int)maxValue;
                }
                if("产油".equals(type)){
                    re_maps.put(Integer.toString(year), oils);
                } else if ("产水".equals(type)){
                    re_maps.put(Integer.toString(year), waters);
                }
                else if ("产气".equals(type)){
                    re_maps.put(Integer.toString(year), gases);
                }
                else if ("产液".equals(type)){
                    re_maps.put(Integer.toString(year), liquids);
                }
                else if ("含水".equals(type)){
                    re_maps.put(Integer.toString(year), rates);
                }
                else if ("总计".equals(type)){
                    re_maps.put(Integer.toString(year), totals);
                }
            }
            tag += 1;
            if("月".equals(year_choose) && tag == 1){
                break;
            }
        }
        re_maps.put("xais", xais);
        re_maps.put("max", max);
        model.addAttribute("yields", yields);
        return re_maps;
    }

    @GetMapping("/search")
    @ResponseBody
    public LayuiUtils<List<Yield>> search(String name, String startDate,  String endDate,
                                          @RequestParam(name="page",required = true,defaultValue = "1")int page,
                                          @RequestParam(name="limit",required = true,defaultValue = "15")int size) throws ParseException {
        System.out.println(name + startDate + ", " +endDate);
        Yield yield = new Yield();
        //设置参数
        yield.setWname(name);
        Date startYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDate);
        Date endYear = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDate);
        LambdaQueryWrapper<Yield> lambdaQueryWrapper = new LambdaQueryWrapper<Yield>()
                .eq(Yield::getWname, name)
                .between(Yield::getTime, startYear, endYear);
        //进行分页查询
        Page<Yield> pageinfo = new Page<Yield>(page, size);
        yieldService.page(pageinfo, lambdaQueryWrapper);
        //打印封装数据
        LayuiUtils<List<Yield>> result = new LayuiUtils<List<Yield>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }

    @PostMapping("/importExcel")
    public LayuiUtils<Yield> importExcel(MultipartFile uploadFile) {
        System.out.println("开始导入 Excel 文件...");
        System.out.println("上传的文件对象：" + uploadFile.toString()); // 打印上传的文件对象
        System.out.println("上传的文件名：" + uploadFile.getOriginalFilename()); // 打印上传的文件名
        Yield yield = new Yield();
        try {
            // 解析 Excel 文件
            // 解析 Excel 文件
            Workbook workbook;
            if (uploadFile.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(uploadFile.getInputStream());
            } else if (uploadFile.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(uploadFile.getInputStream());
            } else {
                throw new RuntimeException("Excel 文件格式错误");
            }
            Sheet sheet = workbook.getSheetAt(0);
            // 遍历 Excel 表格
            LambdaQueryWrapper<Yield> queryWrapper = new LambdaQueryWrapper<>();
            //添加排序条件，根据sort排序
            queryWrapper.ge(Yield::getId,0);
            //进行分页查询
            List<Yield> yields = yieldService.list(queryWrapper);
            // 由于使用自增id方式时会出现重复id的问题, 获取总数目
            int index = yields.size();
            for (Row row : sheet) {
                //忽略表头
                if (row.getRowNum() == 0) {
                    continue;
                }

                // 将单元格的值赋值给实体类对应的属性
                yield.setId(++index);
                yield.setWid((int)Double.parseDouble(getCellValue(row.getCell(1))));
                yield.setWname(getCellValue(row.getCell(2)));
                yield.setLiquid(Double.parseDouble(getCellValue(row.getCell(3))));
                yield.setOil(Double.parseDouble(getCellValue(row.getCell(4))));
                yield.setWater(Double.parseDouble(getCellValue(row.getCell(5))));
                yield.setGas(Double.parseDouble(getCellValue(row.getCell(6))));
                yield.setRate(Double.parseDouble(getCellValue(row.getCell(7))));
                yield.setTime(DateUtils.stringToDate(getCellValue(row.getCell(8))));
                yield.setPtime(Double.parseDouble(getCellValue(row.getCell(9))));
                System.out.println(yield.toString());
                // 保存学生信息到数据库
                yieldService.save(yield);
            }
            System.out.println("Excel 文件导入完成。");
            return new LayuiUtils<Yield>("上传成功", yield,1,0);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excel 文件导入失败。");
            //打印封装数据
            return new LayuiUtils<Yield>("上传失败", yield,0,0);
        }
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期类型，转化为字符串
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                } else {
                    // 如果是数值类型，转化为字符串
                    cell.getNumericCellValue();
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // 尝试获取公式计算结果
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }
}
