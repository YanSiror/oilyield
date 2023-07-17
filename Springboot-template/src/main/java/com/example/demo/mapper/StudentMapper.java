package com.example.demo.mapper;

import com.example.demo.bean.Student;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author: vof
 * @description:
 * @create: 2022-05-12 17:37
 * ElasticsearchRepository<T, ID> T：实体类泛型，ID：实体类主键类型
 **/
public interface StudentMapper extends ElasticsearchRepository<Student, String> {
    @Query("{\"suggest\": {\"prefix\": ?0, \"completion\": {\"field\": \"suggest\"}}}")
    Student suggest(String prefix);
}
