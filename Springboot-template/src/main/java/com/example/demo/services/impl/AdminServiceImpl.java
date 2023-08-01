package com.example.demo.services.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.bean.Admin;
import com.example.demo.bean.Staff;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper,Admin> implements AdminService {
    @Override
    public Boolean findByEmail(String email) {
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<Admin>();
        queryWrapper.eq(Admin::getEmail,email);
        return this.getOne(queryWrapper) != null;
    }
}
