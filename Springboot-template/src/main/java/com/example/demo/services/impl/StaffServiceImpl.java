package com.example.demo.services.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.bean.Staff;
import com.example.demo.bean.User;
import com.example.demo.mapper.StaffMapper;
import com.example.demo.services.StaffService;
import com.example.demo.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 删除所有 id
     * @param ids
     */
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
                redisUtils.delete("staffCache::" + id);
            }
        }else {
            this.removeById(Integer.parseInt(ids));
            redisUtils.delete("staffCache::" + ids);
        }
    }

    @Override
    public Boolean findByEmail(String email) {
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<Staff>();
        queryWrapper.eq(Staff::getEmail,email);
        return this.getOne(queryWrapper) != null;
    }

    @Override
    public Boolean findByPhone(String phone) {
        LambdaQueryWrapper<Staff> queryWrapper = new LambdaQueryWrapper<Staff>();
        queryWrapper.eq(Staff::getPhone,phone);
        return this.getOne(queryWrapper) != null;
    }
}
