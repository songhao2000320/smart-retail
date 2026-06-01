package com.retail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.retail.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(@Param("username") String username);

    List<User> selectList(@Param("role") String role, @Param("offset") int offset, @Param("limit") int limit);

    int countList(@Param("role") String role);

    int updateRole(@Param("userId") Long userId, @Param("role") String role);

    int updateLoginFail(@Param("userId") Long userId, @Param("count") int count,
                        @Param("status") Integer status, @Param("lockedUntil") String lockedUntil);

    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
