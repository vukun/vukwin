package com.njupt.gmall.user.mapper;

import com.njupt.gmall.bean.UmsMember;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author zhaokun
 * @create 2020-05-11 20:38
 */
public interface UserMapper extends Mapper<UmsMember> {

    int judgeUserIsExsitByPhone(@Param("username") String username, @Param("phone") String phone);

    int checkUsername(@Param("username") String username);

    int checkPhone(@Param("phone") String phone);

}
