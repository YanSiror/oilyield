package com.example.demo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.io.Serializable;

/**
 * Well Pojo
 * @author: JingYan
 * @Time 18/4/2023
 */
public class Well implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;
    private Double pressure;
    private Double diameter;
    private Double deep;
    private Double stroke;
    private Double times;
    private Double displacement;


    public Well(Integer id, String name, Double pressure, Double diameter, Double deep, Double stroke, Double times, Double displacement) {
        this.id = id;
        this.name = name;
        this.pressure = pressure;
        this.diameter = diameter;
        this.deep = deep;
        this.stroke = stroke;
        this.times = times;
        this.displacement = displacement;
    }

    public Well() {
    }

    @Override
    public String toString() {
        return "Well{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pressure=" + pressure +
                ", diameter=" + diameter +
                ", deep=" + deep +
                ", stroke=" + stroke +
                ", times=" + times +
                ", displacement=" + displacement +
                '}';
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) {
        this.diameter = diameter;
    }

    public Double getDeep() {
        return deep;
    }

    public void setDeep(Double deep) {
        this.deep = deep;
    }

    public Double getStroke() {
        return stroke;
    }

    public void setStroke(Double stroke) {
        this.stroke = stroke;
    }

    public Double getTimes() {
        return times;
    }

    public void setTimes(Double times) {
        this.times = times;
    }

    public Double getDisplacement() {
        return displacement;
    }

    public void setDisplacement(Double displacement) {
        this.displacement = displacement;
    }
}
