package cc.nanoic.yuna.common.security.filter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.context.SecurityContextHolder;
import cc.nanoic.yuna.common.security.config.JwtProperties;
import cc.nanoic.yuna.common.security.utils.JwtUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InnerAuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 从请求头中获取 userId 和 username
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userId = httpRequest.getHeader(SecurityConstants.DETAILS_USER_ID);
        String username = httpRequest.getHeader(SecurityConstants.DETAILS_USERNAME);

        // 从网关中获取Header参数
        if (StrUtil.isNotBlank(userId)) {
            SecurityContextHolder.LoginUser loginUser = new SecurityContextHolder.LoginUser();
            loginUser.setUserId(Long.valueOf(userId));
            loginUser.setUsername(URLDecoder.decode(username, StandardCharsets.UTF_8));
            // TODO: 解析角色权限（待完善）
            SecurityContextHolder.set(loginUser);
        } else { // 没有网关Header，尝试直接解析 JWT
            String token = httpRequest.getHeader(jwtProperties.getTokenHeader());
            
            if (StrUtil.isNotBlank(token) && token.startsWith(jwtProperties.getTokenHead())) {
                String authToken = token.substring(jwtProperties.getTokenHead().length());
                String usernameFromToken = jwtUtil.getUsernameFromToken(authToken);
                Long userIdFromToken = jwtUtil.getUserIdFromToken(authToken);

                if (usernameFromToken != null && jwtUtil.validateToken(authToken, usernameFromToken, "access")) {
                    SecurityContextHolder.LoginUser loginUser = new SecurityContextHolder.LoginUser();
                    loginUser.setUserId(userIdFromToken);
                    loginUser.setUsername(usernameFromToken);
                    // TODO: 加载权限
                    // loginUser.setPermissions(...);
                    SecurityContextHolder.set(loginUser);
                }
            }
        }

        // 继续执行过滤器链
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.remove();
        }

    }

}
