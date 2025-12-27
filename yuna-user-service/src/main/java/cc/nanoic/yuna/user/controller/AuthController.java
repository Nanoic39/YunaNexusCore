package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.user.model.dto.UserCodeLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.vo.UserLoginVO;
import cc.nanoic.yuna.user.service.AuthService;
import cc.nanoic.yuna.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestParam("email") String email) {
        authService.sendEmailVerifyCode(email);
        return R.success(null, "验证码已发送，请注意查收");
    }

    @PostMapping("/login")
    public R<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        return R.success(userService.login(loginDTO));
    }

    @PostMapping("/login/code")
    public R<UserLoginVO> loginByCode(@RequestBody UserCodeLoginDTO loginDTO) {
        // 先验证验证码
        if (!authService.verifyCode(loginDTO.getEmail(), loginDTO.getCode())) {
            return R.fail("验证码错误或已失效");
        }
        return R.success(userService.loginByEmail(loginDTO.getEmail()));
    }
}
