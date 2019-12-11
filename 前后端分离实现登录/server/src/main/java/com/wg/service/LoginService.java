package com.wg.service;

import com.wg.po.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wanggang.io
 * @Date: 2019/12/2 9:49
 * @todo
 */
@Service
public class LoginService {

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.online}")
    private String onlineKey;

    @Autowired
    RedisTemplate redisTemplate;

    public SysUser save(String userName, String token, HttpServletRequest request){
        SysUser sysUser = new SysUser();
        sysUser.setUserName(userName);
        redisTemplate.opsForValue().set(onlineKey + token, sysUser);
        redisTemplate.expire(onlineKey + token,expiration, TimeUnit.MILLISECONDS);
        return sysUser;
    }
    //校验用户名及密码
    public boolean validNameAndPass(String userName,String password){
        if(!(userName.equals("admin") && password.equals("123456")))
            return false;
        return true;
    }
}
