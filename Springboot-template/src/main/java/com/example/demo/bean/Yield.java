package com.example.demo.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author VOF
 * @since 2023-04-16
 */
public class Yield implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer wid;
    private String wname;
    private Double liquid;
    private Double oil;
    private Double water;
    private Double gas;
    private Double rate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private Double ptime;
    @TableField(exist = false)
    private int status;

    public Yield() {
    }

    public Yield(Integer id, Integer wid, String wname, Double liquid, Double oil, Double water, Double gas, Double rate, Date time, Double ptime, int status) {
        this.id = id;
        this.wid = wid;
        this.wname = wname;
        this.liquid = liquid;
        this.oil = oil;
        this.water = water;
        this.gas = gas;
        this.rate = rate;
        this.time = time;
        this.ptime = ptime;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Yield{" +
                "id=" + id +
                ", wid=" + wid +
                ", wname='" + wname + '\'' +
                ", liquid=" + liquid +
                ", oil=" + oil +
                ", water=" + water +
                ", gas=" + gas +
                ", rate=" + rate +
                ", time=" + time +
                ", ptime=" + ptime +
                ", status=" + status +
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

    public Integer getWid() {
        return wid;
    }

    public void setWid(Integer wid) {
        this.wid = wid;
    }

    public String getWname() {
        return wname;
    }

    public void setWname(String wname) {
        this.wname = wname;
    }

    public Double getLiquid() {
        return liquid;
    }

    public void setLiquid(Double liquid) {
        this.liquid = liquid;
    }

    public Double getOil() {
        return oil;
    }

    public void setOil(Double oil) {
        this.oil = oil;
    }

    public Double getWater() {
        return water;
    }

    public void setWater(Double water) {
        this.water = water;
    }

    public Double getGas() {
        return gas;
    }

    public void setGas(Double gas) {
        this.gas = gas;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Double getPtime() {
        return ptime;
    }

    public void setPtime(Double ptime) {
        this.ptime = ptime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
