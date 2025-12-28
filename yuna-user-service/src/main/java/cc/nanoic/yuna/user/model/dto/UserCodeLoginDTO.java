package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class UserCodeLoginDTO {
    /**
     * 邮箱
     */
    private String email;

    /**
     * 验证码
     */
    private String code;

    /**
     * 用户名 (自动注册时必填)
     */
    private String username;

    /**
     * 密码 (自动注册时必填)
     */
    private String password;

    /**
     * 昵称（可选，不填则为默认）
     */
    private String nickname;

    /**
     * 性别（0:未知 1:男 2:女，可选，不填则为未知）
     */
    private Integer gender;
}
