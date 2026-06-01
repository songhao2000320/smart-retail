package com.retail.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_zone")
public class Zone extends BaseEntity {

    private Long storeId;
    private String name;
    private Double posX;
    private Double posY;
    private Double width;
    private Double height;
    private Integer status;

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPosX() { return posX; }
    public void setPosX(Double posX) { this.posX = posX; }

    public Double getPosY() { return posY; }
    public void setPosY(Double posY) { this.posY = posY; }

    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
