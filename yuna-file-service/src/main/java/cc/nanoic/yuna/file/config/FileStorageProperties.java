package cc.nanoic.yuna.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yuna.file")
public class FileStorageProperties {
    private String rootPath = "data/files";
    private long maxSizeBytes = 1L * 1024 * 1024 * 1024 * 1024; // 最大单个文件大小1TB
    private long base64MaxBytes = 2L * 1024 * 1024; // 最大Base64编码文件大小2MB
    private String uuidSecret;
    private Integer workerId;
}