package cc.nanoic.yuna.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yuna.jwt")
public class JwtProperties {
    /**
     * 密钥
     */
    private String secret;
    
    /**
     * AccessToken 过期时间（秒），默认 2 小时
     */
    private Long accessExpiration = 7200L;

    /**
     * RefreshToken 过期时间（秒），默认 7 天
     */
    private Long refreshExpiration = 604800L;

    /**
     * Token 前缀
     */
    private String tokenHead = "Bearer";

    /**
     * Token 请求头字段
     */
    private String tokenHeader = "Authorization";
}