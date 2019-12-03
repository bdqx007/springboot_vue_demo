package com.wg.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysUser implements Serializable {
    private Long id;

    private String nickName;

    private String userName;

    private String password;

    private Integer userType;

    private Date creatTime;

    private Integer logicState;





}