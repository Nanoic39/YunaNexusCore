package cc.nanoic.yuna.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import cc.nanoic.yuna.common.security.config.JwtProperties;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("cc.nanoic.yuna")
@EnableConfigurationProperties(JwtProperties.class)
public class YunaFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(YunaFileApplication.class, args);
    }
}