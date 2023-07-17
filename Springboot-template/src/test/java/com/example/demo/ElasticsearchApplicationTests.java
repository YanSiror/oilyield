package com.example.demo;

import com.example.demo.bean.Student;
import com.example.demo.mapper.StudentMapper;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexInformation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.index.MappingBuilder;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
class ElasticsearchApplicationTests {
//    @Autowired
//    private ElasticsearchClient client;
    @Resource
    StudentMapper studentMapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    void contextLoads3() {

        // 新增单个文档
//        Student student = new Student("1", "张三", "男", 18, "张三");
//        studentMapper.save(student);

        List<Student> students = Arrays.asList(
                new Student("1", "张三", "男", 18),
                new Student("2", "李四", "男", 20),
                new Student("3", "王五", "女", 22),
                new Student("4", "张楠", "女", 18)
                );
        students.forEach(student -> {
            List<String> suggestList = Collections.singletonList(student.getName());
            student.setSuggest(new Completion(suggestList.toArray(new String[0])));
        });

        studentMapper.saveAll(students);

//        // 批量新增文档
//        List<Student> studentList = new ArrayList<>();
//        studentList.add(new Student("1", "张三", "男", 18, new Completion("张三".split(""))));
//        studentList.add(new Student("2", "李四", "男", 20, new Completion("李四".split(""))));
//        studentList.add(new Student("3", "王五", "女", 22, new Completion("王五".split(""))));
//        studentMapper.saveAll(studentList);

        Optional<Student> optionalStudent = studentMapper.findById("1");
        System.out.println(optionalStudent.get());
    }

    @Test
    void test02() {
        String keyword = "张";
        // 使用suggest进行标题联想
        CompletionSuggestionBuilder suggest = SuggestBuilders.completionSuggestion("suggest")
                // 关键字（参数传此）
                .prefix(keyword)
                // 重复过滤
                .skipDuplicates(true)
                // 匹配数量
                .size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my-suggest", suggest);

        IndexCoordinates indexCoordinates = elasticsearchOperations.getIndexCoordinatesFor(Student.class);

        // 查询
        SearchResponse goodsNameSuggestResp = elasticsearchRestTemplate.suggest(suggestBuilder, indexCoordinates);
        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> goodsNameSuggest = goodsNameSuggestResp
                .getSuggest().getSuggestion("my-suggest");

        // 处理返回
        List<String> suggests = goodsNameSuggest.getEntries().stream().map(x -> x.getOptions().stream().map(y -> y.getText().toString()).collect(Collectors.toList())).findFirst().get();
        // 输出内容
        for (String su : suggests) {
            System.out.println("suggest = " + su);
        }
    }


}