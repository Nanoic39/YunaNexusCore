package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;
import cc.nanoic.yuna.user.entity.Permission;
import cc.nanoic.yuna.user.model.dto.PermissionDTO;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import cc.nanoic.yuna.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @RequiresPermissions("sys:permission:list")
    public R<List<PermissionVO>> tree() {
        return R.success(permissionService.getPermissionTree());
    }

    @PostMapping
    @RequiresPermissions("sys:permission:add")
    public R<Void> add(@RequestBody PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permissionService.save(permission);
        return R.success(null, "添加权限成功");
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:permission:edit")
    public R<Void> update(@PathVariable("id") Long id, @RequestBody PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setId(id);
        permissionService.updateById(permission);
        return R.success(null, "修改权限成功");
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:permission:delete")
    public R<Void> delete(@PathVariable("id") Long id) {
        permissionService.removeById(id);
        return R.success(null, "删除权限成功");
    }
}
