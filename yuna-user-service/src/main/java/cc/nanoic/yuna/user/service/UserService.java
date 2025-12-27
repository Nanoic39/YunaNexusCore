package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
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
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    /**
     * 邮箱验证码登录
     * @param email 邮箱
     * @return 登录结果
     */
    UserLoginVO loginByEmail(String email);
}
