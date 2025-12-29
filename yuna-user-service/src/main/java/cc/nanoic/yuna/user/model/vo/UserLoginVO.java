package cc.nanoic.yuna.user.model.vo;

import cc.nanoic.yuna.user.entity.UserInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    /**
     * 用户UUID(这是外界和系统交互的用户唯一标识)
     */
    private String uuid;

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

    /**
     * 用户信息, 包含用户的基本信息
     */
    private UserInfoVO userInfo;
}
