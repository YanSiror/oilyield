package com.example.demo.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Component;

// indexName名字如果是字母那么必须是小写字母
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "student")
public class Student {

    @Id
    @Field(store = true, type = FieldType.Keyword)
    private String id;

    @Field(store = true, type = FieldType.Text, analyzer = "ik_smart")
    private String name;

    @Field(store = true, type = FieldType.Text)
    //Text可以分词 ik_smart=粗粒度分词 ik_max_word 为细粒度分词
    private String sex;

    @Field(index = false, store = true, type = FieldType.Integer)
    private Integer age;

    @CompletionField(analyzer="ik_smart",searchAnalyzer="ik_smart", maxInputLength = 100)
    private Completion suggest;

    public Student(String id, String name,  String sex, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
    }
}
