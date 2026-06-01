package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.entity.ZoneStay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ZoneStayMapper extends BaseMapper<ZoneStay> {

    /** 根据行为ID查询所有区域停留记录（含区域名称） */
    List<ZoneStay> selectByBehaviorId(@Param("behaviorId") Long behaviorId);
}
