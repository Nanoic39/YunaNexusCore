package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.user.model.dto.UserChangeEmailDTO;
import cc.nanoic.yuna.user.model.dto.UserChangePasswordDTO;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.model.dto.UserUpdateDTO;
import cc.nanoic.yuna.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

}