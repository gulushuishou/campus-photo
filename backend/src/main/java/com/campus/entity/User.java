package com.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    /** 校区 */
    private String campus;

    /** 角色：USER / PHOTOGRAPHER / ADMIN */
    private String role;

    /** 信用积分，初始 100 */
    private Integer creditScore;

    /** 状态：0 正常 1 封禁 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
