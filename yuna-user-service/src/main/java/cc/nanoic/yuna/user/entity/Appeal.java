package cc.nanoic.yuna.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appeal")
public class Appeal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String contact;
    private String reason;
    private Integer status;
    private Long operatorId;
    private String processRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
