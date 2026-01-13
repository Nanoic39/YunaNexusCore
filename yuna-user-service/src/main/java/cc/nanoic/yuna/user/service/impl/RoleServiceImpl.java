package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.user.entity.Role;
import cc.nanoic.yuna.user.entity.RolePermission;
import cc.nanoic.yuna.user.mapper.RoleMapper;
import cc.nanoic.yuna.user.mapper.RolePermissionMapper;
import cc.nanoic.yuna.user.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionMapper rolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // Delete existing
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        // Add new
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> list = permissionIds.stream().map(pid -> {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                return rp;
            }).collect(Collectors.toList());
            
            for (RolePermission rp : list) {
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());
    }
}
