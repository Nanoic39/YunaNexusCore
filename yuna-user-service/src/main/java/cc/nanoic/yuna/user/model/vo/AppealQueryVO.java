package cc.nanoic.yuna.user.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppealQueryVO {
    private Long id;
    private String contact;
    private String reason;
    private Integer status;
    private Long operatorId;
    private String processRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String uuid;
    private boolean userExist;
}
