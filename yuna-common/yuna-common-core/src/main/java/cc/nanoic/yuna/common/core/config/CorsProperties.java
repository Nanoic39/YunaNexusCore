package cc.nanoic.yuna.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * CORS 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yuna.cors")
public class CorsProperties {

    /**
     * 是否开启 CORS
     */
    private Boolean enabled = true;

    /**
     * 拦截路径
     */
    private String path = "/**";

    /**
     * 允许的域名列表，使用 List<String> 以便配置多个
     * 例如：http://localhost:3000, https://example.com
     */
    private List<String> allowedOrigins = Collections.singletonList("*");

    /**
     * 允许的方法
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    /**
     * 允许的头信息
     */
    private List<String> allowedHeaders = Collections.singletonList("*");

    /**
     * 是否允许发送 Cookie
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求的缓存时间（秒）
     */
    private Long maxAge = 3600L;
}
