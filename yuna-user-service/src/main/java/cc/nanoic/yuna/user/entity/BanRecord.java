package cc.nanoic.yuna.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ban_record")
public class BanRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer banType;
    private Integer banRange;
    private String banService;
    private String banReason;
    private Long banOperatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long unbanOperatorId;
}

