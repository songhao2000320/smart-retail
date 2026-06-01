package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.entity.UserStoreAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserStoreAuthMapper extends BaseMapper<UserStoreAuth> {

    /**
     * 获取用户授权的门店列表（含门店名称）
     */
    List<Map<String, Object>> selectByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有授权
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 获取有某门店授权的用户ID列表
     */
    List<Long> selectUserIdsByStoreId(@Param("storeId") Long storeId);
}
