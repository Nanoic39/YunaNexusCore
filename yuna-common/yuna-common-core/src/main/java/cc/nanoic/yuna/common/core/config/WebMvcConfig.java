package cc.nanoic.yuna.common.core.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private CorsProperties corsProperties;

    @Override
    @SuppressWarnings("null")
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        if (!Boolean.TRUE.equals(corsProperties.getEnabled())) {
            return;
        }

        String path = Optional.ofNullable(corsProperties.getPath()).orElse("/**");
        
        List<String> allowedOrigins = Optional.ofNullable(corsProperties.getAllowedOrigins())
                .orElse(Collections.singletonList("*"));
        
        List<String> allowedMethods = Optional.ofNullable(corsProperties.getAllowedMethods())
                .orElse(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        List<String> allowedHeaders = Optional.ofNullable(corsProperties.getAllowedHeaders())
                .orElse(Collections.singletonList("*"));

        registry.addMapping(path)
                .allowedOriginPatterns(allowedOrigins.toArray(String[]::new))
                .allowedMethods(allowedMethods.toArray(String[]::new))
                .allowedHeaders(allowedHeaders.toArray(String[]::new))
                .allowCredentials(Boolean.TRUE.equals(corsProperties.getAllowCredentials()))
                .maxAge(Optional.ofNullable(corsProperties.getMaxAge()).orElse(3600L));
    }
}
