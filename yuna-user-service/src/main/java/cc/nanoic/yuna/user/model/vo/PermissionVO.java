package cc.nanoic.yuna.user.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PermissionVO {
    private Long id;
    private Long parentId;
    private Integer resourceType;
    private String permCode;
    private String permName;
    private LocalDateTime createdAt;
    
    private List<PermissionVO> children;
}
