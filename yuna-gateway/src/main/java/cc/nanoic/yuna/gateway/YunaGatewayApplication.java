package cc.nanoic.yuna.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import cc.nanoic.yuna.common.security.config.JwtProperties;

// 网关排除不需要的 DataSource，防止数据库影响
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@ComponentScan("cc.nanoic.yuna")
@EnableConfigurationProperties(JwtProperties.class)
public class YunaGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(YunaGatewayApplication.class, args);
    }   
}
