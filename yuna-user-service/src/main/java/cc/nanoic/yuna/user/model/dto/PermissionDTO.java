package cc.nanoic.yuna.user.model.dto;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long parentId;
    private Integer resourceType;
    private String permCode;
    private String permName;
}
