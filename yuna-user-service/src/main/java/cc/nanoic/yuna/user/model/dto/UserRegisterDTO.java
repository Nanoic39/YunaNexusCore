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
     * 确认密码
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

}
