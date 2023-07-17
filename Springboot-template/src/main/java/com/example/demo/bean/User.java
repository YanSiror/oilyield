package com.example.demo.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    Integer id;             //编号
    String name;        //名字
    String sex;         //性别
    int age;            //年龄
    String phone;       //电话
    String email;       //邮箱
    String password;    //密码

}
