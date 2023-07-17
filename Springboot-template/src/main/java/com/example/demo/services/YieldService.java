package com.example.demo.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Yield;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author VOF
 * @since 2023-04-16
 */
public interface YieldService extends IService<Yield> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
