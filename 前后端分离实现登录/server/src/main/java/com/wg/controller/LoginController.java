package com.wg.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.wf.captcha.ArithmeticCaptcha;
import com.wg.config.ResultMsg;
import com.wg.po.SysUser;
import com.wg.service.LoginService;
import com.wg.service.RedisServiceImpl;

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
    @Value("${jwt.privateKey}")
    private String privateKey;
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
        //校验验证码
        if(StringUtil.isNullOrEmpty(validCode))
            return ResultMsg.getFailedMsg("验证码已过期");
        if(!validCode.equals(code))
            return ResultMsg.getFailedMsg("验证码错误");

        //用私钥解密
        password = new String(RSAEncrypt.decrypt(password,privateKey));
        if(!loginService.validNameAndPass(userName,password))
            return ResultMsg.getFailedMsg("用户名或密码错误");

        //登录成功，生成token
        String token = jwtTokenUtil.generateToken(userName);
        //保存登录用户信息
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
        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        captcha.setLen(2);
        String result = captcha.text();
        String uuid = codeKey + IdUtil.simpleUUID();
        redisService.saveCode(uuid,result);
        JSONObject json = new JSONObject();
        json.put("img",captcha.toBase64());
        json.put("uuid",uuid);
        return ResultMsg.getMsg(json);
    }
}
