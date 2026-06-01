package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.entity.Zone;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZoneMapper extends BaseMapper<Zone> {

    List<Zone> selectByStoreId(@Param("storeId") Long storeId);

    int deleteByStoreId(@Param("storeId") Long storeId);
}
