package com.example.demo.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author VOF
 * @since 2023-07-14
 */
public interface UserService extends IService<User> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);

    /**
     * 根据邮箱查找
     * @param email
     * @return
     */
    public Boolean findByEmail(String email);

    /**
     * 根据电话查找
     * @param phone
     * @return
     */
    public Boolean findByPhone(String phone);
}
