package com.example.demo.services;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.News;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author VOF
 * @since 2023-05-05
 */
public interface NewsService extends IService<News> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
