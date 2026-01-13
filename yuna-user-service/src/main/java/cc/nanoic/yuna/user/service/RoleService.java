package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoleService extends IService<Role> {
    void assignPermissions(Long roleId, List<Long> permissionIds);
    List<Long> getRolePermissions(Long roleId);
}
