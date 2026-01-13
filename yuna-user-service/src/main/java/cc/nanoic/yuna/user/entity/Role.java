package cc.nanoic.yuna.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;

    private String roleName;

    private Integer roleLevel;

    private String description;

    private LocalDateTime createdAt;
}
