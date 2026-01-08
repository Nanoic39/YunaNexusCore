package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.user.model.dto.UserCodeLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.vo.UserLoginVO;
import cc.nanoic.yuna.user.service.AuthService;
import cc.nanoic.yuna.user.service.UserService;
import cc.nanoic.yuna.common.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final JwtUtil jwtUtil;

    /**
     * 注册
     * 
     * @param registerDTO 注册信息
     * @return 注册成功的用户ID
     */
    @PostMapping("/register")
    public R<Long> register(@RequestBody UserRegisterDTO registerDTO) {
        return R.success(userService.register(registerDTO));
    }

    /**
     * 发送验证码
     * 
     * @param email 目标邮箱
     * @return 成功消息
     */
    @PostMapping("/send-code")
    public R<Void> sendCode(@RequestParam("email") String email) {
        authService.sendEmailVerifyCode(email);
        return R.success(null, "验证码已发送，请注意查收");
    }

    /**
     * 密码登录
     * 
     * @param loginDTO 包含用户名/邮箱和密码的登录DTO
     * @return 登录成功后的用户VO
     */
    @PostMapping("/login")
    public R<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        return R.success(userService.login(loginDTO));
    }

    /**
     * 通过验证码登录 (自动注册)
     * 
     * @param loginDTO 包含邮箱和验证码的登录DTO
     * @return 登录成功后的用户VO
     */
    @PostMapping("/login/code")
    public R<UserLoginVO> loginByCode(@RequestBody UserCodeLoginDTO loginDTO) {
        return R.success(userService.loginByEmail(loginDTO));
    }

    /**
     * 检查邮箱是否已注册
     * 
     * @param email 邮箱
     * @return true: 已注册, false: 未注册
     */
    @GetMapping("/check-email")
    public R<Boolean> checkEmail(@RequestParam("email") String email) {
        return R.success(userService.checkEmail(email));
    }

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的登录信息
     */
    @GetMapping("/refresh")
    public R<UserLoginVO> refresh(@RequestParam("refreshToken") String refreshToken) {
        return R.success(userService.refreshAccessToken(refreshToken));
    }

    /**
     * 校验当前 AccessToken 是否有效
     * 
     */
    @GetMapping("/validate")
    public R<Void> validate(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return R.fail(cc.nanoic.yuna.common.core.result.ResultCode.UN_AUTHORIZED, "Authorization header missing",
                    "登录状态失效，请重新登录");
        }

        String token = auth.substring(7);
        boolean ok = jwtUtil.validateToken(token, null, "access");
        if (!ok) {
            return R.fail(cc.nanoic.yuna.common.core.result.ResultCode.UN_AUTHORIZED, "Token invalid", "登录状态失效，请重新登录");
        }
        return R.success();
    }
}
