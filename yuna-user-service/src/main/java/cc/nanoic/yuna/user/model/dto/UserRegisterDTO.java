package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 邮箱验证码
     */
    private String verifyCode;

    /**
     * 昵称（可选，不填则为默认）
     */
    private String nickname;

    /**
     * 性别（0:未知 1:男 2:女，可选，不填则为未知）
     */
    private Integer gender;

}
