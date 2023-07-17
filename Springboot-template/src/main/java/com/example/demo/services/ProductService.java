package com.example.demo.services;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Product;

public interface ProductService extends IService<Product> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
