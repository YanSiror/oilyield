package com.example.demo.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.bean.Admin;
import com.example.demo.bean.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.services.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author VOF
 * @since 2023-07-14
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void deleteSelected(String ids) {
        if (ids.contains(",")) {
            String[] split = ids.split(",");
            List<Integer> list = new ArrayList<Integer>();
            for (String s : split) {
                int i = 0;
                try {
                    i = Integer.parseInt(s);
                } catch (NumberFormatException e) {

                }
                list.add(i);
            }
            for (int id : list) {
                this.removeById(id);
            }
        }else {
            this.removeById(Integer.parseInt(ids));
        }
    }

    @Override
    public Boolean findByEmail(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getEmail,email);
        return this.getOne(queryWrapper) != null;
    }

    @Override
    public Boolean findByPhone(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getPhone,phone);
        return this.getOne(queryWrapper) != null;
    }
}
