package cc.nanoic.yuna.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("yuna_file_share")
public class YunaFileShare {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String fileUuid;
    private Long userId;

    private String shareToken;
    private String sharePwd;

    private Integer permissionType;

    private Integer downloadLimit;
    private Integer downloadCount;

    private LocalDateTime expireTime;

    private Integer status;

    private Long createBy;
    private LocalDateTime createTime;

    private Long updateBy;
    private LocalDateTime updateTime;
}