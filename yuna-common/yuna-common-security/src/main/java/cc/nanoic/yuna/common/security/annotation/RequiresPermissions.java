package cc.nanoic.yuna.common.security.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 权限校验注解
 * 用于方法或类上，校验当前用户是否有执行该方法或访问该类的权限
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

    /**
     * 需要校验的权限码
     * 
     * @return
     */
    String[] value();

    /**
     * 校验逻辑，默认AND
     * 
     * @return
     */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR, NOT
    }

}
