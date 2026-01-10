package cc.nanoic.yuna.file.model.dto;

import lombok.Data;

@Data
public class ShareCreateDTO {
    private String fileUuid;
    private String sharePwd;
    private Integer permissionType;
    private Integer downloadLimit;
    private Long expireSeconds;
}