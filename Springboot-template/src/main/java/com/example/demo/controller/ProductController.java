package com.example.demo.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Product;
import com.example.demo.services.ProductService;
import com.example.demo.utils.FileUploadUtils;
import com.example.demo.utils.LayuiUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * ProductController
 * @author: JingYan
 * @Time 18/3/2023
 */
@RequestMapping("/product")
@Controller
@Api(tags = "产品")
public class ProductController {
    @Autowired
    private ProductService productService;
    String filePath = String.valueOf(Paths.get(FileUploadUtils.realPath, "products"));


    @PostMapping("/save")
    @ResponseBody
    public LayuiUtils<List<Product>> save(Product object){
        System.out.println("save:"+object.toString());
        object.setImg(object.getImg());
        productService.save(object);
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("1", null,1,0);
        return result;
    }

    @PutMapping("/modify")
    @ResponseBody
    public LayuiUtils<List<Product>> modify(Product product){
        System.out.println("modify:"+product.toString());
        productService.updateById(product);
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("1", null,0,0);
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
    public LayuiUtils<List<Product>> deleteSelected(@RequestParam(value = "id", defaultValue = "") String ids) throws Exception {
        productService.deleteSelected(ids);
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("1", null,0,0);
        return result;
    }

    @GetMapping("/loadData/{id}")
    public ModelAndView loadData(@PathVariable("id") int id){
        //type 用来控制返回页面的类型
        ModelAndView mv = new ModelAndView();
        Product product = productService.getById(id);
        //设置模型
        mv.addObject("product", JSON.toJSONString(product));
        System.out.println(JSON.toJSONString(product));
        //设置视图
        mv.setViewName("product-modify");
        return mv;
    }

    @GetMapping("/delete")
    @ResponseBody
    public LayuiUtils<List<Product>> delete(@RequestParam(name="id",required = true)String id) {
        System.out.println("delete:"+id);
        productService.deleteSelected(id);
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("1", null,0,0);
        return result;
    }

    @GetMapping("/toAdd")
    public String toAdd(){
        return "product-add";
    }


    @GetMapping("/toList")
    public String toList(Admin admin, Model model, HttpSession session){
        return "product-list";
    }

    /**
     * 采用分页代码方法
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/list_front")
    @ResponseBody
    public LayuiUtils<List<Product>> listFront(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "3")int limit) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(Product::getName);
        //进行分页查询
        Page<Product> pageinfo = new Page<Product>(page, limit);
        productService.page(pageinfo, queryWrapper);
        System.out.println(page + "  " + limit);
        System.out.println(pageinfo.getRecords());

        for (Product product: pageinfo.getRecords()) {
            String[] split = product.getImg().split("\\\\");
            String name = split[split.length - 1];
            System.out.println(name);
            product.setImg(name);
        }

        System.out.println(pageinfo.toString());
        System.out.println(pageinfo.getRecords());
        mv.addObject("products", pageinfo.getRecords());
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    /**
     * 遍历所有的产量信息, 分页
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    @ResponseBody
    public LayuiUtils<List<Product>> list(@RequestParam(name="page",required = true,defaultValue = "1")int page,@RequestParam(name="limit",required = true,defaultValue = "3")int size) {
        ModelAndView mv = new ModelAndView();
        //条件构造器对象
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort排序
        queryWrapper.orderByAsc(Product::getName);
        //进行分页查询
        Page<Product> pageinfo = new Page<Product>(page, size);
        productService.page(pageinfo,queryWrapper);
        System.out.println(page + "  " + size);
        System.out.println(pageinfo.getRecords());

        System.out.println(pageinfo.toString());
        mv.addObject("products", pageinfo.getRecords());

        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("", pageinfo.getRecords(),0,(int)pageinfo.getTotal());
        return result;
    }

    @GetMapping("/search")
    @ResponseBody
    public LayuiUtils<List<Product>> search(String name){
        System.out.println("search name:  "+name);
        Product product = new Product();
        //设置参数
        product.setName(name);
        LambdaQueryWrapper<Product> lambdaQueryWrapper = new LambdaQueryWrapper<Product>();
        lambdaQueryWrapper.like(name != null, Product::getName,name);
        List<Product> productList = productService.list(lambdaQueryWrapper);
        //使用PageInfo包装数据
        PageInfo<Product> pageInfo = new PageInfo<Product> (productList,3);
        //打印封装数据
        LayuiUtils<List<Product>> result = new LayuiUtils<List<Product>>("", productList,0,(int)pageInfo.getTotal());
        System.out.println(JSON.toJSONString(result));
        return result;
    }

    /**
     * 保存图片
     * @param id
     * @param filename
     * @return
     */
    @PostMapping("/saveHeader")
    @ResponseBody
    public LayuiUtils<Product> saveHeader(String id, String filename){
        Product product = productService.getById(id);
        System.out.println("filename:" + filename);
        product.setImg(filename);
        productService.updateById(product);
        System.out.println(product.toString());
        //打印封装数据
        return new LayuiUtils<Product>("保存成功", product,1,0);
    }


    /**
     * 获取图片
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
        Product product = productService.getById(Integer.parseInt(id));
        System.out.println(product.getImg());
        // 创建文件路径
        Path path = Paths.get(filePath, product.getImg());
        System.out.println(path);

        File sourceimage = new File(String.valueOf(path));
        BufferedImage image = ImageIO.read(sourceimage);

        //将内存中的图片输出到浏览器
        //参数一：图片对象
        //参数二：图片的格式，如PNG,JPG,GIF
        //参数三：图片输出到哪里去
        ImageIO.write(image,"PNG",response.getOutputStream());
    }

    /**
     * 上传文件
     * @param uploadFile
     * @throws IOException
     */
    @PostMapping("/uploadHead")
    public void uploadHead(MultipartFile uploadFile) throws IOException{
        //生成文件夹, 如果 module文件夹不存在则创建文件夹
        File file=new File(filePath);
        if(!file.exists()){
            file.mkdir();
        }

        String name;        //文件名
        //获取上传文件的名称
        name = uploadFile.getOriginalFilename();
        System.out.println(uploadFile);
        System.out.println(name);
        //保存到固定位置
        Path path = Paths.get(filePath, name);
        uploadFile.transferTo(new File(String.valueOf(path)));

//        String name;        //文件名
//        String src;         //全部路径
//        //获取上传文件的名称
//        name = uploadFile.getOriginalFilename();
//        System.out.println(uploadFile);
//        System.out.println(name);
//        src = filePath+name;
//        //保存到固定位置
//        File sourceFile = new File(src);
//        uploadFile.transferTo(sourceFile);
//
//
//        String serverpath = ResourceUtils.getURL("").getPath();
//        System.out.println(serverpath);
//        String targetPath = serverpath + "src/main/resources/static/assets/pimgs";
//        targetPath = targetPath.substring(1, targetPath.length());
//        System.out.println(targetPath);
//
//        File targetDir = new File(targetPath);
//        if (!targetDir.exists()) {
//            targetDir.mkdirs(); // 如果目标路径不存在，则创建目标路径
//        }
//        File targetFile = new File(targetDir.getAbsolutePath() + "/" + name);
//        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("文件复制成功");
//
//        System.out.println(targetPath);
//        // 将上传的文件保存到 assets 目录下
//        //uploadFile.transferTo(new File(rootPath + "\\pimgs\\" + name));
    }
}
