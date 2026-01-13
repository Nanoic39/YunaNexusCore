package cc.nanoic.yuna.common.security.aspect;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import cc.nanoic.yuna.common.core.context.SecurityContextHolder;
import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;

@Aspect
@Component
public class PreAuthorizeAspect {
    
    @Pointcut("@annotation(cc.nanoic.yuna.common.security.annotation.RequiresPermissions)")
    public void pointcut() {
    }

    /**
     * 环绕通知，用于处理权限验证
     * @param point 连接点，用于获取方法信息
     * @return 方法执行结果
     * @throws Throwable 方法执行过程中可能抛出的异常
     */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        // 获取方法上的注解
        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
        // 如果方法上没有注解，则直接执行方法
        if (annotation == null) {
            return point.proceed();
        }

        String[] requiredPermissions = annotation.value();
        RequiresPermissions.Logical logical = annotation.logical();

        // 校验权限
        checkPermissions(requiredPermissions, logical);

        return point.proceed();
    }

    private boolean permissionMatches(String ownedPermission, String requiredPermission) {
        if (ownedPermission == null || ownedPermission.isEmpty() || requiredPermission == null || requiredPermission.isEmpty()) {
            return false;
        }
        // 完全放行
        if ("*:*:*".equals(ownedPermission)) {
            return true;
        }
        // 分段匹配（最多三段），段内支持 * 作为通配
        String[] ownedSegs = ownedPermission.split(":");
        String[] reqSegs = requiredPermission.split(":");
        int len = Math.max(ownedSegs.length, reqSegs.length);
        // 规范为三段
        len = Math.min(len, 3);
        for (int i = 0; i < len; i++) {
            String o = i < ownedSegs.length ? ownedSegs[i] : "";
            String r = i < reqSegs.length ? reqSegs[i] : "";
            if ("*".equals(o)) {
                continue;
            }
            if (!o.equals(r)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPermission(Set<String> userPermissions, String requiredPermission) {
        for (String owned : userPermissions) {
            if (permissionMatches(owned, requiredPermission)) {
                return true;
            }
        }
        return false;
    }

    private void checkPermissions(String[] requiredPermissions, RequiresPermissions.Logical logical) {
        // 校验权限逻辑
        SecurityContextHolder.LoginUser loginUser = SecurityContextHolder.get();

        // 空权限判断
        if (loginUser == null) {
            throw new BusinessException("未登录或Token失效");
        }
        Set<String> userPermissions = loginUser.getPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            // 这里判断的是任何权限都没有的情况，即使是系统超级管理员，至少也会有一条“*:*:*”权限
            throw new BusinessException("无访问权限");
        }

        // 放行超级管理员（三段通配）
        if (userPermissions.contains("*:*:*")) {
            return;
        }

        // 根据条件判断是否满足权限要求
        boolean hasPermission = false;
        if (logical == RequiresPermissions.Logical.AND) {
            hasPermission = true;
            // 循环判断所有权限是否都满足
            for (String permission : requiredPermissions) {
                if (!hasPermission(userPermissions, permission)) {
                    // AND条件下只要有一个权限不满足，就算条件不满足，直接跳出循环
                    hasPermission = false;
                    break;
                }
            }
        } else if (logical == RequiresPermissions.Logical.OR) {
            for (String permission : requiredPermissions) {
                if (hasPermission(userPermissions, permission)) {
                    // OR条件下只要有一个权限满足，就算条件满足，直接跳出循环
                    hasPermission = true;
                    break;
                }
            }
        } else if (logical == RequiresPermissions.Logical.NOT) {
            hasPermission = true;
            for (String permission : requiredPermissions) {
                if (hasPermission(userPermissions, permission)) {
                    // NOT条件下只要存在指定权限，就算条件不满足，直接跳出循环
                    hasPermission = false;
                    break;
                }
            }
        }

        // 判断完毕
        if (!hasPermission) {
            throw new BusinessException("无访问权限");
        }

    }
}
