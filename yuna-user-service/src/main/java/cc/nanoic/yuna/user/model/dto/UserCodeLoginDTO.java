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
}
