package cc.nanoic.yuna.common.security.service;

import java.util.Set;

/**
 * 安全权限服务接口
 * 用于在过滤器中加载用户权限
 */
public interface SecurityPermissionService {
    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限标识集合
     */
    Set<String> getPermissions(Long userId);

    /**
     * 检查用户是否被封禁
     * @param userId 用户ID
     * @return 是否封禁
     */
    boolean isUserBanned(Long userId);
}
