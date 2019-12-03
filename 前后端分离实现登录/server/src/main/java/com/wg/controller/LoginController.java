package com.wg.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.wf.captcha.ArithmeticCaptcha;
import com.wg.config.ResultMsg;
import com.wg.po.SysUser;
import com.wg.service.LoginService;
import com.wg.service.RedisServiceImpl;
import com.wg.utils.EncryptUtils;
import com.wg.utils.JwtTokenUtil;
import com.wg.utils.RSAEncrypt;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: wanggang.io
 * @Date: 2019/12/2 9:38
 * @todo
 */
@RestController
@RequestMapping("/auth")
public class LoginController {
    @Value("${jwt.codeKey}")
    private String codeKey;
    @Autowired
    private RedisServiceImpl redisService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private LoginService loginService;
    @PostMapping(value = "/login")
    public ResultMsg login(@RequestBody JSONObject loginInfo,HttpServletRequest request) throws Exception{
        String userName = loginInfo.getString("username");
        String password = loginInfo.getString("password");
        String uuid = loginInfo.getString("uuid");
        String code = loginInfo.getString("code");
        //请求信息为空校验
        if(StringUtil.isNullOrEmpty(userName)|| StringUtil.isNullOrEmpty(password))
            return ResultMsg.getFailedMsg("用户名密码不能为空");
        if(StringUtil.isNullOrEmpty(uuid) || StringUtil.isNullOrEmpty(code))
            return ResultMsg.getFailedMsg("验证码错误");

        //redis 查询验证码
        String validCode = redisService.getCodeVal(uuid);
        //redis 清除验证码
        redisService.delete(uuid);
        //check valid code
        if(StringUtil.isNullOrEmpty(validCode))
            return ResultMsg.getFailedMsg("验证码已过期");
        if(!validCode.equals(code))
            return ResultMsg.getFailedMsg("验证码错误");
        //check username and password here
        String pass1 = EncryptUtils.encryptPassword("123");

        String privateKey ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIZrND5fJTS048AIIhrXfAyqyeKHHcS25YEynvRPzVLFopBTBGEEirI9csH/J9FL4ML8cy3oXP6DHD9VVlEq+5g2liOjkTPWrPb9Oj4Mua0Rahj5FhNjXSQTaMGMfGvNywvRzZd4SJFNpC9vqVyLT8IjLg3I13DyebHqDw+7jUThAgMBAAECgYAd9JQe9jsnZMyAhg97pdvqQsFc7zmOFihNQ4ey8HnUYWDaAvYt3MI/+HuzifZIqT08lt/gMjFPMs6/unfS/N42eNbuUjyNs2UDmKSzGaDcY3dtES1kZT0qh5bZ5WSAQNYqJfmaVGps3Y80rxdRK9xJ9+d5injRsRlpdV0SZyjphQJBANslYPUgm5i3M+F8VcuIkZPB1yKDJUCE6Kjv2MWSv6vNBy7b14QOYshmpknPrYF27ekMFdcEbOm5fO8kfH326QsCQQCdBiscm2ZXDETA1c2ANOArZoRUay8DvV2HuGNZBXF+sRq8txhQRbpX8jZtRySdZETpq5euMapX9YkeoSvEbTVDAkBNX5YrFhc5xT1RGJgmI5Laq27s5Ybqj8KhmwVbRKPd4abumXovSvD/tpZxqxykgDwhsz2mzA40O2Rr/uLhiqLfAkA/tqEHmoaMC45aMGaR/uJ2ucI3/sW03sF0sHyfQnYq0fHnYOOcrq5NwRoUfSl+M5FCvz1skPptdlOM+hioOyfBAkEAnVhR3H5FEBHBrP0P8iWrldNVGw06yecuVjROqKoXdOqv2eIi3vHCoOQ/4wEAo7667qoLAoNBEvBi+GbzC6WJ2w==";
        String key2 = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMa6bUdZRwqS1pzWruHBZSlzOTvpxq1vcqireYqNE+8MDz1Rc+G6AZDi9waZkJyVm2q7jQB7aZRoZzALq7w8v8fyr7ggjDa+6iJgA41zxFdFnQIcfOdzp3Hfs9ufOd8NVa5Mf6Ve9KXv+hxIH+KazPyK+P+FNqY5AYrzBI3dbgOPAgMBAAECgYEAiV9foR2W3TlJ/5rrwwUEIBYVb1QX0dYjhXMbjmodaHaGplWC7Hu/D1/FcRuAeq10hOBnBPlKr5tIEU9QBMxdqOqpiYSRtOX24FdJKhBsiezw7G6x0ze7CBE1C3XvHqwTDkA/U9VnR9xWFsz9Ks8GSgFchxJaCkRJpriF2+iPPOkCQQDwEqKpqUHIpoOdIXycxS3DLiWDGGxSjQYJHwXySgT+ST2Q8UFqf5yLuHRVsbA0/teFRjzFEvChPQQDBy3rUyPbAkEA0+mZ0Vl97ct4ZCHw3qUBtTqzzWJpy4f4NZmBD1q0q/ZwlvPUBh2LEbtc8WWDPj+ytS6een4Ef9NWgMzwYm0HXQJBAK8g6QhbMGHvFpPJmd3C+V8oirTXXC3cMkr1FqFo3buiMgdJ9y55aPmD1VmuBZyjSxUt56bb6i21FPgghJ7mR4ECQForNeYcfDboswt4XbN+5qEkn0kvLPELpBO6g23zHJlnPTUd/wOzIm+jF8MnfJbKJ9JsSca5RFTXqiEYEtjup4kCQQCPYlaDmctcgT8teyrVcdolfaZJJNtS9Lg5aiUyBWr1wj9aeqbisNDB+OINBV+DYfpg7uhNaY0dYLrfwdEKy2ZJ";

        password = new String(RSAEncrypt.decrypt(password,key2));
        if(!(userName.equals("admin") && password.equals("123456")))
            return ResultMsg.getFailedMsg("用户名或密码错误");
        String token = jwtTokenUtil.generateToken(userName);
        //save online user info
        SysUser user = loginService.save(userName,token,request);
        JSONObject json = new JSONObject();
        json.put("token",token);
        json.put("user",user);
        return ResultMsg.getMsg(json);
    }

    @PostMapping(value = "/loginOut")
    public ResultMsg loginOut(HttpServletRequest request){


        return null;
    }
    @GetMapping(value = "/code")
    public ResultMsg getCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        // 获取运算的结果：5
        String result = captcha.text();
        String uuid = codeKey + IdUtil.simpleUUID();
        redisService.saveCode(uuid,result);
        JSONObject json = new JSONObject();
        json.put("img",captcha.toBase64());
        json.put("uuid",uuid);
        return ResultMsg.getMsg(json);
    }
}
