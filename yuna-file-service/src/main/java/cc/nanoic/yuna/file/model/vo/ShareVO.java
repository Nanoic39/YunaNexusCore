package cc.nanoic.yuna.file.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareVO {
    private String shareToken;
    private String fileUuid;
    private Integer permissionType;
    private Integer downloadLimit;
    private Integer downloadCount;
    private LocalDateTime expireTime;
    private Boolean needPwd;
    private String fileName;
    private Integer status;
}