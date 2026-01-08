package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class UserChangePasswordDTO {
    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}