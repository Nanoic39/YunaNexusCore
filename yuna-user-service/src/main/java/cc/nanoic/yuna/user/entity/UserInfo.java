package cc.nanoic.yuna.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;

@Data
@TableName("user_info")
public class UserInfo {

    /**
     * 关联yuna_user的id(一对一,注册时自动创建)
     */
    @TableId("user_id")
    private Long userId;

    /**
     * 昵称(最长64字符)
     */
    private String nickname;

    /**
     * 头像uuid(最长36字符)
     */
    @TableField("avatar_id")
    private String avatarId;

    /**
     * 性别(0:未知,1:男,2:女)
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    /**
     * 个人简介(最长255字符)
     */
    private String biography;

    /**
     * 经验值(int)
     */
    private Integer experience;

    /**
     * 信息更新时间戳
     */
    private LocalDateTime updateTime;
    
}
