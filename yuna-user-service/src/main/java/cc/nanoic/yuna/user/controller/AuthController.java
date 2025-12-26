package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestParam("email") String email) {
        authService.sendEmailVerifyCode(email);
        return R.success(null, "验证码已发送，请注意查收");
    }
}