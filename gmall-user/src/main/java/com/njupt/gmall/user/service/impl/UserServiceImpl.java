package com.njupt.gmall.user.service.impl;

import com.njupt.gmall.user.mapper.UserMapper;
import com.njupt.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhaokun
 * @create 2020-05-11 20:36
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;
}
