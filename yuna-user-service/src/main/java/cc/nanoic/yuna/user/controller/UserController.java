package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
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
}