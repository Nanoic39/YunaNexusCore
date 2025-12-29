package cc.nanoic.yuna.user;

import cc.nanoic.yuna.common.security.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("cc.nanoic.yuna")
@EnableConfigurationProperties(JwtProperties.class)
public class YunaUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(YunaUserApplication.class, args);
    }
}