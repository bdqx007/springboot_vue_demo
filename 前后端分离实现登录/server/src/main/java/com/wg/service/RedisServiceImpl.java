package com.wg.service;

import com.wg.po.RedisVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class RedisServiceImpl  {

    private final RedisTemplate redisTemplate;

    @Value("${loginCode.expiration}")
    private Long expiration;

    @Value("${jwt.online}")
    private String onlineKey;

    @Value("${jwt.codeKey}")
    private String codeKey;

    public RedisServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }



    public List<RedisVo> findByKey(String key) {
        List<RedisVo> redisVos = new ArrayList<>();
        if(!"*".equals(key)){
            key = "*" + key + "*";
        }
        Set<String> keys = redisTemplate.keys(key);
        for (String s : keys) {
            RedisVo redisVo = new RedisVo(s, Objects.requireNonNull(redisTemplate.opsForValue().get(s)).toString());
            redisVos.add(redisVo);
        }
        return redisVos;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void deleteAll() {
        Set<String> keys = redisTemplate.keys(  "*");
        redisTemplate.delete(keys.stream().filter(s -> !s.contains(onlineKey)).filter(s -> !s.contains(codeKey)).collect(Collectors.toList()));
    }

    public String getCodeVal(String key) {
        try {
            return Objects.requireNonNull(redisTemplate.opsForValue().get(key)).toString();
        }catch (Exception e){
            return "";
        }
    }

    public void saveCode(String key, Object val) {
        redisTemplate.opsForValue().set(key,val);
        redisTemplate.expire(key,expiration, TimeUnit.MINUTES);
    }


}
