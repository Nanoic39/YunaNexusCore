package cc.nanoic.yuna.user.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * Token (暂未实现，预留)
     */
    private String token;
}
