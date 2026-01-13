package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;
import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.entity.BanRecord;
import cc.nanoic.yuna.user.service.BanRecordService;
import cc.nanoic.yuna.user.model.dto.UserChangeEmailDTO;
import cc.nanoic.yuna.user.model.dto.UserChangePasswordDTO;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.model.dto.UserUpdateDTO;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import cc.nanoic.yuna.user.model.vo.RoleVO;
import cc.nanoic.yuna.user.model.vo.UserVO;
import cc.nanoic.yuna.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BanRecordService banRecordService;

    /**
     * 更新用户信息
     * 
     * @param userId    从网关透传的用户ID
     * @param updateDTO 更新信息
     * @return 成功
     */
    @PostMapping("/update")
    public R<Void> updateUserInfo(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUserInfo(userId, updateDTO);
        return R.success(null, "信息更新成功");
    }

    /**
     * 获取用户个人信息
     * 
     * @param userId 从网关透传的用户ID
     * @param uuid   用户唯一标识（可选）
     * @return 用户个人信息
     */
    @GetMapping("/profile")
    public R<UserDetailDTO> getUserProfile(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam(value = "uuid", required = false) String uuid) {
        // 如果传了uuid，获取指定用户的信息；否则获取当前登录用户的信息
        // 如果没有透传 userId (未登录) 且没有传 uuid -> 报错
        if (userId == null && uuid == null) {
            throw new BusinessException(ResultCode.FAILURE, "缺少必要参数");
        }

        // 如果传了uuid，获取指定用户的信息；否则获取当前登录用户的信息
        Long targetUserId = uuid != null ? userService.getUserIdByUuid(uuid) : userId;
        UserDetailDTO userProfile = userService.getUserDetail(targetUserId);

        return R.success(userProfile);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public R<Void> changePassword(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestBody UserChangePasswordDTO changePasswordDTO) {
        userService.changePassword(userId, changePasswordDTO);
        return R.success(null, "密码修改成功");
    }

    /**
     * 修改邮箱
     */
    @PostMapping("/change-email")
    public R<Void> changeEmail(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestBody UserChangeEmailDTO changeEmailDTO) {
        userService.changeEmail(userId, changeEmailDTO);
        return R.success(null, "邮箱修改成功");
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:user:list")
    public R<Page<UserVO>> list(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }
        Page<User> result = userService.page(userPage, wrapper);

        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserVO> voList = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return R.success(voPage);
    }

    /**
     * 获取封禁用户列表
     */
    @GetMapping("/ban/list")
    @RequiresPermissions("sys:ban:list")
    public R<Page<UserVO>> bannedList(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<User> userPage = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 2);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }
        Page<User> result = userService.page(userPage, wrapper);
        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<UserVO> voList = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return R.success(voPage);
    }

    /**
     * 封禁用户
     */
    @PostMapping("/{id}/ban")
    @RequiresPermissions("sys:user:ban")
    public R<Void> banUser(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "range", required = false) Integer range,
            @RequestParam(value = "service", required = false) String service,
            @RequestParam(value = "reason", required = false) String reason) {
        if (operatorId != null && operatorId.equals(id)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "禁止对自己执行封禁操作");
        }
        boolean targetIsSuper = userService.isSuperAdmin(id);
        boolean operatorIsSuper = userService.isSuperAdmin(operatorId);
        if (targetIsSuper && !operatorIsSuper) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权操作超级管理员账号");
        }
        userService.updateStatus(id, 2);
        banRecordService.recordBan(id, operatorId, type, range, service, reason);
        return R.success(null, "封禁成功");
    }

    /**
     * 解封用户
     */
    @PostMapping("/{id}/unban")
    @RequiresPermissions("sys:user:unban")
    public R<Void> unbanUser(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id) {
        if (operatorId != null && operatorId.equals(id)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "禁止对自己执行解封操作");
        }
        boolean targetIsSuper = userService.isSuperAdmin(id);
        boolean operatorIsSuper = userService.isSuperAdmin(operatorId);
        if (targetIsSuper && !operatorIsSuper) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权操作超级管理员账号");
        }
        userService.updateStatus(id, 1);
        banRecordService.closeOpenBans(id, operatorId);
        return R.success(null, "解封成功");
    }

    /**
     * 查询用户封禁记录
     */
    @GetMapping("/{id}/ban/records")
    @RequiresPermissions("sys:ban:list")
    public R<Page<BanRecord>> banRecords(@PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<BanRecord> p = new Page<>(page, size);
        LambdaQueryWrapper<BanRecord> qw = new LambdaQueryWrapper<BanRecord>()
                .eq(BanRecord::getUserId, id)
                .orderByDesc(BanRecord::getStartTime);
        Page<BanRecord> result = banRecordService.page(p, qw);
        return R.success(result);
    }

    /**
     * 分配角色
     */
    @PostMapping("/{userId}/roles")
    @RequiresPermissions("sys:user:assign")
    public R<Void> assignRoles(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("userId") Long userId, @RequestBody List<Long> roleIds) {
        if (operatorId != null && operatorId.equals(userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "禁止修改自己的角色");
        }
        boolean targetIsSuper = userService.isSuperAdmin(userId);
        boolean operatorIsSuper = userService.isSuperAdmin(operatorId);
        if (targetIsSuper && !operatorIsSuper) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权操作超级管理员账号");
        }
        userService.assignRoles(userId, roleIds);
        return R.success(null, "分配角色成功");
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/{userId}/roles")
    @RequiresPermissions("sys:user:list")
    public R<List<RoleVO>> getUserRoles(@PathVariable("userId") Long userId) {
        return R.success(userService.getUserRoles(userId));
    }

    /**
     * 管理员获取用户详情
     */
    @GetMapping("/{id}")
    @RequiresPermissions("sys:user:list")
    public R<UserDetailDTO> getUserDetailById(@PathVariable("id") Long id) {
        return R.success(userService.getUserDetail(id));
    }

    /**
     * 获取当前用户的权限
     */
    @GetMapping("/permissions")
    public R<List<PermissionVO>> getCurrentUserPermissions(
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        return R.success(userService.getUserPermissions(userId));
    }
}
