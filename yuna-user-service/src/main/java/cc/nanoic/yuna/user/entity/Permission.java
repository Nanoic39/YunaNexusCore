package cc.nanoic.yuna.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;

    private Integer resourceType;

    private String permCode;

    private String permName;

    private LocalDateTime createdAt;
}
