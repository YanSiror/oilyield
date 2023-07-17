package com.example.demo.bean;

public class Product {
    Integer id;          //编号
    String name;    //产品名
    double price;     //产品价格
    String infor;     //产品信息
    String img;       //产品图片


    public Product() {
    }

    public Product(int id, String name, double price, String infor, String img) {
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

    public void setId(int id) {
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

    public void setPrice(double price) {
        this.price = price;
    }

    public String getInfor() {
        return infor;
    }

    public void setInfor(String infor) {
        this.infor = infor;
    }
}
