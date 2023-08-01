package com.example.demo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * Admin Pojo
 * @author: JingYan
 * @Time 18/3/2023
 */
public class Admin {
    private int id;
    private String username;
    private String password;
    private String email;
    /**
     头像
     */
    String header;

    public Admin() {
    }

    public Admin(int id, String username, String password, String email, String header) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.header = header;
    }

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Admin(int id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public Admin(int id, String header) {
        this.id = id;
        this.header = header;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", header='" + header + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}

