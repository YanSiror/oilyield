package com.example.demo.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Well;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author VOF
 * @since 2023-04-16
 */
public interface WellService extends IService<Well> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
