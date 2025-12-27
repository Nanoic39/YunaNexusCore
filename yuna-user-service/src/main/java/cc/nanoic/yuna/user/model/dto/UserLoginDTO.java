package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    /**
     * 用户名/邮箱
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}
