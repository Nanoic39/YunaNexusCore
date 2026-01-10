package cc.nanoic.yuna.file.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileMetaVO {
    private Long id;
    private String uuid;
    private Long userId;
    private Long folderId;

    private String originName;
    private String fileName;

    private String filePath;

    private Integer storageType;

    private Long fileSize;
    private String fileType;
    private String mimeType;

    private String identifier;

    private Integer category;

    private Integer isFolder;
    private Integer fileCount;
    private Long subSize;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer status;

    private Boolean isShared;
}