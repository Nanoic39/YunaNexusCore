package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.model.dto.UserCodeLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import cc.nanoic.yuna.user.model.dto.UserUpdateDTO;
import cc.nanoic.yuna.user.model.vo.UserLoginVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册成功的用户ID
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 账号密码登录
     * 
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    /**
     * 邮箱验证码登录
     * 
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO loginByEmail(UserCodeLoginDTO loginDTO);

    /**
     * 检查邮箱是否已注册
     * 
     * @param email 邮箱
     * @return true: 已注册, false: 未注册
     */
    boolean checkEmail(String email);

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 刷新后的登录结果
     */
    UserLoginVO refreshAccessToken(String refreshToken);

    /**
     * 更新用户信息
     * 
     * @param userId    用户ID
     * @param updateDTO 更新信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);
}
