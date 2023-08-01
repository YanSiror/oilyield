package com.example.demo.services;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Admin;

public interface AdminService extends IService<Admin> {
    /**
     * 根据邮箱查找
     * @param email
     * @return
     */
    public Boolean findByEmail(String email);
}
