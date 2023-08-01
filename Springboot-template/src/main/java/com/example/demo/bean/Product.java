package com.example.demo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Product Pojo
 * @author: JingYan
 * @Time 18/7/2023
 */
@TableName("product")
public class Product {
    private Integer id;          //编号
    private String name;    //产品名
    private Double price;     //产品价格
    private String infor;     //产品信息
    private String img;       //产品图片


    public Product() {
    }

    public Product(Integer id, String name, Double price, String infor, String img) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.infor = infor;
        this.img = img;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", infor='" + infor + '\'' +
                ", img='" + img + '\'' +
                '}';
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getInfor() {
        return infor;
    }

    public void setInfor(String infor) {
        this.infor = infor;
    }
}
