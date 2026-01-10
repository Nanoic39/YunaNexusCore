package cc.nanoic.yuna.file.model.vo;

import lombok.Data;

@Data
public class FileUploadVO {
    private String uuid;
    private String originName;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer storageType;
    private Integer category;
}