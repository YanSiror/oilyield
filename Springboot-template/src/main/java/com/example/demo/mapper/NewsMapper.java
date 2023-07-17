package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.bean.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author VOF
 * @since 2023-05-05
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

}
