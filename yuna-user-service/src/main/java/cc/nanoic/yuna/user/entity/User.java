package cc.nanoic.yuna.user.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("user")
public class User {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外显唯一标识(固定36字符)
     */
    private String uuid;

    /**
     * 用户名(最大64字符)
     */
    private String username;

     /**
      * 密码Argon2/BCrypt加密储存(最大64字符)
      */
    private String password;

    /**
     * 邮箱(最大128字符)
     */
    private String email;

    /**
     * 帐号状态(0：注销，1：正常，2：封禁/冻结)
     */
    private Integer status;

    /**
     * 创建时间(默认格式为13位时间戳)
     */
    private LocalDateTime createTime;




}
