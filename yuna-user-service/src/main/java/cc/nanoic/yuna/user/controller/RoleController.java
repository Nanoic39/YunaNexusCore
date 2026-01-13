package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;
import cc.nanoic.yuna.user.entity.Role;
import cc.nanoic.yuna.user.model.dto.RoleDTO;
import cc.nanoic.yuna.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/list")
    @RequiresPermissions("sys:role:list")
    public R<List<Role>> list() {
        return R.success(roleService.list());
    }

    @PostMapping
    @RequiresPermissions("sys:role:add")
    public R<Void> add(@RequestBody RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        roleService.save(role);
        return R.success(null, "添加角色成功");
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:role:edit")
    public R<Void> update(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setId(id);
        roleService.updateById(role);
        return R.success(null, "修改角色成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:role:delete")
    public R<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        return R.success(null, "删除角色成功");
    }

    @PostMapping("/{roleId}/permissions")
    @RequiresPermissions("sys:role:assign")
    public R<Void> assignPermissions(@PathVariable("roleId") Long roleId, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(roleId, permissionIds);
        return R.success(null, "分配权限成功");
    }

    @GetMapping("/{roleId}/permissions")
    @RequiresPermissions("sys:role:list")
    public R<List<Long>> getRolePermissions(@PathVariable("roleId") Long roleId) {
        return R.success(roleService.getRolePermissions(roleId));
    }
}
