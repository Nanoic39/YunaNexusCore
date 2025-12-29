package cc.nanoic.yuna.common.core.context;

import java.util.Set;

import com.alibaba.ttl.TransmittableThreadLocal;

import lombok.Data;

/**
 * 安全上下文持有者
 * 用于存储和获取当前线程的安全上下文
 */
public class SecurityContextHolder {
    
    /**
     * 登录用户上下文
     */
    private static final TransmittableThreadLocal<LoginUser> THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置登录用户上下文
     * @param loginUser
     */
    public static void set(LoginUser loginUser) {
        THREAD_LOCAL.set(loginUser);
    }

    /**
     * 获取登录用户上下文
     * @return
     */
    public static LoginUser get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 移除登录用户上下文
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

    /**
     * 获取登录用户ID
     * @return Long 用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取登录用户名
     * 
     * @return String 用户名
     */
    public static String getUsername() {
        LoginUser loginUser = get();
        return loginUser != null ? loginUser.getUsername() : null;
    }


    /**
     * 登录用户上下文
     */
    @Data
    public static class LoginUser {
        private Long userId;
        private String username;
        private Set<String> roles;
        private Set<String> permissions;
    }
}
