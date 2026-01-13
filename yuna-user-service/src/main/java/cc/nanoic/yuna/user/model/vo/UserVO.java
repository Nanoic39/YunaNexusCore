package cc.nanoic.yuna.user.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String uuid;
    private String username;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
}
