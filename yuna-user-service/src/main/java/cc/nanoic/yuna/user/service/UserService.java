package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册成功的用户ID
     */
    Long register(UserRegisterDTO registerDTO);
}