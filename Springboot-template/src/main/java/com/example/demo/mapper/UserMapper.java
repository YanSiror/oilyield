package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.bean.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author VOF
 * @since 2023-07-14
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
