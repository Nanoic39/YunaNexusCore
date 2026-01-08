package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class UserChangeEmailDTO {
    /**
     * 新邮箱
     */
    private String newEmail;

    /**
     * 新邮箱验证码
     */
    private String newEmailCode;

    /**
     * 旧邮箱验证码（如果有旧邮箱）
     */
    private String oldEmailCode;
}