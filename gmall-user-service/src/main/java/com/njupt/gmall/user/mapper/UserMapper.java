package com.njupt.gmall.user.mapper;

import com.njupt.gmall.bean.UmsMember;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author zhaokun
 * @create 2020-05-11 20:38
 */
public interface UserMapper extends Mapper<UmsMember> {

    UmsMember judgeUserIsExsitByPhone(@Param("phone") String phone, @Param("username") String username);

}
