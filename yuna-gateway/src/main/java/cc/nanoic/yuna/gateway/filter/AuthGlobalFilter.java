package cc.nanoic.yuna.gateway.filter;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.security.config.JwtProperties;
import cc.nanoic.yuna.common.security.utils.JwtUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // TODO: 白名单路径
    private static final List<String> IGNORE_URLS = List.of(
            "/api/user/auth/login", // 登录接口
            "/api/user/auth/login/code", // 验证码登录接口
            "/api/user/auth/register", // 注册接口
            "/api/user/auth/send-code", // 发送验证码接口
            "/api/user/auth/check-email", // 校验邮箱接口
            "/api/user/auth/refresh", // 刷新token接口
            "/api/user/auth/validate" // 校验token接口
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 对OPTIONS请求放行(预检请求使用,否则会拦截请求无法进入后续跨域处理逻辑,导致前端预检请求失败)
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 对白名单放行
        for (String ignoreUrl : IGNORE_URLS) {
            if (pathMatcher.match(ignoreUrl, path)) {
                return chain.filter(exchange);
            }
        }

        // 获取 Token
        String token = request.getHeaders().getFirst(jwtProperties.getTokenHeader());
        if (StrUtil.isBlank(token) || !token.startsWith(jwtProperties.getTokenHead())) {
            return unauthorized(exchange);
        }

        String authToken = token.substring(jwtProperties.getTokenHead().length());

        // 校验 Token (必须是 access 类型)
        // 这里 username 传 null，只校验 Token 有效性和类型
        if (!jwtUtil.validateToken(authToken, null, "access")) {
            return unauthorized(exchange);
        }

        // 解析用户信息并透传 Header
        Long userId = jwtUtil.getUserIdFromToken(authToken);
        String username = jwtUtil.getUsernameFromToken(authToken);

        if (userId == null || username == null) {
            return unauthorized(exchange);
        }

        // 构建新的请求头
        ServerHttpRequest newRequest = request.mutate()
                .header(SecurityConstants.DETAILS_USER_ID, String.valueOf(userId))
                .header(SecurityConstants.DETAILS_USERNAME, URLEncoder.encode(username, StandardCharsets.UTF_8))
                // TODO: 角色和权限信息也应该从 Token 或 Redis 获取并透传
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    /**
     * 返回未授权或Token已失效的 JSON 响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 返回 JSON 格式的 401 响应
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String message = String.format("{\"code\":%d,\"msg\":\"%s\",\"tips\":\"%s\",\"data\":null}",
                HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), "未登录或Token已失效,请登录后重试");
        DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级较高，在 NettyRoutingFilter 之前执行
    }
}