package cc.nanoic.yuna.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("yuna_file_log")
public class YunaFileLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String uuid;

    private Long userId;
    private Long folderId;

    private String originName;
    private String fileName;

    private String filePath;
    private String fileContent;

    private Integer storageType;

    private Long fileSize;
    private String fileType;
    private String mimeType;

    private String identifier;

    private Integer category;

    private Long createBy;
    private LocalDateTime createTime;

    private Long updateBy;
    private LocalDateTime updateTime;

    private Long deleteBy;
    private LocalDateTime deleteTime;

    private Integer status;
}