package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private String roleCode;
    private String roleName;
    private Integer roleLevel;
    private String description;
}
