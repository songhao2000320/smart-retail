package com.retail.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("t_store")
public class Store extends BaseEntity {

    private String name;
    private String address;
    private String phone;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
