package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.entity.Store;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StoreMapper extends BaseMapper<Store> {

    List<Store> selectAll();

    List<Store> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    int countAll();
}
