package com.keymao.security.distributed.uaa.service;


import com.keymao.security.distributed.uaa.dao.UserDao;
import com.keymao.security.distributed.uaa.model.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SpringDataUserDetailsService implements UserDetailsService {

    @Autowired
    UserDao userDao;

    //根据 账号查询用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //将来连接数据库根据账号查询用户信息
        UserDto userDto = userDao.getUserByUsername(username);
        if(userDto == null){
            //如果用户查不到，返回null，由provider来抛出异常
            return null;
        }
        //查询用户权限
        List<String> permissions = userDao.findPermissionsByUserId(userDto.getId());
        //将权限转成数组
        String[] peArrray = new String[permissions.size()];
        permissions.toArray(peArrray);
        //创建userDetails
        UserDetails userDetails = User.withUsername(userDto.getFullname()).password(userDto.getPassword()).authorities(peArrray).build();
        return userDetails;
    }
}
