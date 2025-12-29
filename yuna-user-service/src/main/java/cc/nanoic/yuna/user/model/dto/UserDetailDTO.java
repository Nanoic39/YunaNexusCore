package cc.nanoic.yuna.user.model.dto;

import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.entity.UserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailDTO extends User {
    private UserInfo userInfo;
}