package com.example.demo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.util.Collections;
import java.util.List;

/**
 * WellE Pojo - For elasticSearch document create and utilze
 * @author: JingYan
 * @Time 18/4/2023
 */
@Document(indexName = "well")
public class WellE {
    @Id
    @Field(store = true, type = FieldType.Keyword)
    private Integer id;
    @Field(store = true, type = FieldType.Text, analyzer = "ik_smart")
    private String name;
    @CompletionField(analyzer="ik_smart", searchAnalyzer="ik_smart", maxInputLength = 100)
    private Completion suggest;

    /**
     * 用于自动补全
     * @param id
     * @param name
     * @param suggest
     */
    public WellE(Integer id, String name, Completion suggest) {
        this.id = id;
        this.name = name;
        this.suggest = suggest;
    }

    public WellE(Well well){
        this.id = well.getId();
        this.name = well.getName();
        List<String> suggestList = Collections.singletonList(well.getName());
        this.suggest =  new Completion(suggestList.toArray(new String[0]));;
    }

    public WellE() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Completion getSuggest() {
        return suggest;
    }

    public void setSuggest(Completion suggest) {
        this.suggest = suggest;
    }
}
