package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.bean.Student;
import com.example.demo.bean.Well;
import com.example.demo.bean.WellE;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Mapper
public interface WellEMapper extends ElasticsearchRepository<WellE, String> {

}
