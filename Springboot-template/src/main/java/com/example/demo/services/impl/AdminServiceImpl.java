package com.example.demo.services.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.bean.Admin;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.services.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper,Admin> implements AdminService {

}
