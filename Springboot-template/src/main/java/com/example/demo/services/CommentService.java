package com.example.demo.services;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.bean.Comment;

public interface CommentService extends IService<Comment> {
    /**
     * 删除所有 id
     * @param ids
     */
    void deleteSelected(String ids);
}
