package cc.nanoic.yuna.user.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleVO {
    private Long id;
    private String roleCode;
    private String roleName;
    private Integer roleLevel;
    private String description;
    private LocalDateTime createdAt;
}
