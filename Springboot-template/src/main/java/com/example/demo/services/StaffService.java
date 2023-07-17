package com.example.demo.services;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Staff;

public interface StaffService extends IService<Staff> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
