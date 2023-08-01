package com.example.demo.services;

import com.example.demo.bean.PassForget;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author VOF
 * @since 2023-07-31
 */
public interface PassForgetService extends IService<PassForget> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
